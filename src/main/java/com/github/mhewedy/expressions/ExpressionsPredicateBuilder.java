package com.github.mhewedy.expressions;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.util.Assert;

import java.time.*;
import java.time.chrono.HijrahDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.mhewedy.expressions.Expression.*;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

class ExpressionsPredicateBuilder {

    static <T> Predicate getPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, Expressions expressions) {

        Assert.notNull(expressions, "expressions must not be null!");

        List<Predicate> predicates = getPredicates(query, cb,
                root,
                root.getModel(),
                expressions.getExpressions());

        if (predicates.isEmpty()) {
            return cb.isTrue(cb.literal(true));
        }

        if (predicates.size() == 1) {
            return predicates.iterator().next();
        }

        Predicate[] array = predicates.toArray(new Predicate[0]);

        return cb.and(array);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<Predicate> getPredicates(CriteriaQuery<?> query, CriteriaBuilder cb,
                                                 Path<?> from, ManagedType<?> type,
                                                 List<Expression> expressions) {

        List<Predicate> predicates = new ArrayList<>();

        for (Expression expression : expressions) {

            if (expression instanceof SingularExpression) {

                SingularExpression singularExpression = (SingularExpression) expression;

                final String field = extractField(singularExpression.field);
                Attribute<?, ?> attribute = getAttribute(type, field);

                if (attribute.isAssociation()) {
                    if (attribute instanceof PluralAttribute) {
                        query.distinct(true);
                    }

                    final SubField subField = extractSubField(singularExpression.field);
                    if (!subField.name.isEmpty()) {
                        final SingularExpression subExpression =
                                new SingularExpression(subField.name, singularExpression.operator, singularExpression.value);
                        predicates.addAll(
                                getPredicates(query, cb,
                                        reuseOrCreateJoin((From<?, ?>) from, attribute, field, subField.joinType),
                                        extractSubFieldType(attribute),
                                        singletonList(subExpression)
                                )
                        );
                        continue;
                    }
                }

                Path exprPath = from.get((SingularAttribute) attribute);

                if (PersistentAttributeType.EMBEDDED == attribute.getPersistentAttributeType()) {
                    final SubField subField = extractSubField(singularExpression.field);
                    attribute = extractSubFieldType(attribute).getAttribute(subField.name);
                    exprPath = exprPath.get((SingularAttribute) attribute);
                }

                Object attributeValue = convertValueToAttributeType(singularExpression.value, attribute.getJavaType());
                Predicate predicate;

                switch (singularExpression.operator) {
                    // equality
                    case $eq:
                        if (attributeValue == null) {
                            predicate = cb.isNull(exprPath);
                        } else {
                            predicate = cb.equal(exprPath, attributeValue);
                        }
                        break;
                    case $ieq:
                        predicate = cb.equal(cb.lower(exprPath), ((String) attributeValue).toLowerCase());
                        break;
                    case $ne:
                        if (attributeValue == null) {
                            predicate = cb.isNotNull(exprPath);
                        } else {
                            predicate = cb.notEqual(exprPath, attributeValue);
                        }
                        break;

                    // comparison
                    case $gt:
                        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.gt(exprPath, (Number) attributeValue);
                        } else if (Comparable.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.greaterThan(exprPath, (Comparable) attributeValue);
                        } else {
                            throw new IllegalArgumentException("field should be Number or Comparable: " + singularExpression);
                        }
                        break;
                    case $gte:
                        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.ge(exprPath, (Number) attributeValue);
                        } else if (Comparable.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.greaterThanOrEqualTo(exprPath, (Comparable) attributeValue);
                        } else {
                            throw new IllegalArgumentException("field should be Number or Comparable: " + singularExpression);
                        }
                        break;
                    case $lt:
                        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.lt(exprPath, (Number) attributeValue);
                        } else if (Comparable.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.lessThan(exprPath, (Comparable) attributeValue);
                        } else {
                            throw new IllegalArgumentException("field should be Number or Comparable: " + singularExpression);
                        }
                        break;
                    case $lte:
                        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.le(exprPath, (Number) attributeValue);
                        } else if (Comparable.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.lessThanOrEqualTo(exprPath, (Comparable) attributeValue);
                        } else {
                            throw new IllegalArgumentException("field should be Number or Comparable: " + singularExpression);
                        }
                        break;
                    // like
                    case $start:
                        predicate = cb.like(exprPath.as(String.class), attributeValue + "%");
                        break;
                    case $end:
                        predicate = cb.like(exprPath.as(String.class), "%" + attributeValue);
                        break;
                    case $contains:
                        predicate = cb.like(exprPath.as(String.class), "%" + attributeValue + "%");
                        break;
                    case $istart:
                        predicate = cb.like(cb.lower(exprPath.as(String.class)), attributeValue.toString().toLowerCase() + "%");
                        break;
                    case $iend:
                        predicate = cb.like(cb.lower(exprPath.as(String.class)), "%" + attributeValue.toString().toLowerCase());
                        break;
                    case $icontains:
                        predicate = cb.like(cb.lower(exprPath.as(String.class)), "%" + attributeValue.toString().toLowerCase() + "%");
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + singularExpression);
                }

                predicates.add(predicate);

            } else if (expression instanceof ListExpression) {

                ListExpression listExpression = (ListExpression) expression;
                final String field = extractField(listExpression.field);
                Attribute<?, ?> attribute = getAttribute(type, field);

                if (attribute.isAssociation()) {
                    if (attribute instanceof PluralAttribute) {
                        query.distinct(true);
                    }

                    final SubField subField = extractSubField(listExpression.field);
                    if (!subField.name.isEmpty()) {
                        final ListExpression subExpression =
                                new ListExpression(subField.name, listExpression.operator, listExpression.values);
                        predicates.addAll(
                                getPredicates(query, cb,
                                        reuseOrCreateJoin((From<?, ?>) from, attribute, field, subField.joinType),
                                        extractSubFieldType(attribute),
                                        singletonList(subExpression)
                                )
                        );
                        continue;
                    }
                }

                Path exprPath = from.get((SingularAttribute) attribute);

                if (PersistentAttributeType.EMBEDDED == attribute.getPersistentAttributeType()) {
                    final SubField subField = extractSubField(listExpression.field);
                    attribute = extractSubFieldType(attribute).getAttribute(subField.name);
                    exprPath = exprPath.get((SingularAttribute) attribute);
                }

                List<Object> attributeValues = convertValueToAttributeType(listExpression.values, attribute.getJavaType());

                Predicate predicate;
                switch (listExpression.operator) {
                    // in
                    case $in:
                        CriteriaBuilder.In in = cb.in(exprPath);
                        attributeValues.forEach(in::value);
                        predicate = in;
                        break;
                    case $nin:
                        CriteriaBuilder.In inx = cb.in(exprPath);
                        attributeValues.forEach(inx::value);
                        predicate = cb.not(inx);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + listExpression);
                }
                predicates.add(predicate);

            } else if (expression instanceof OrExpression) {
                predicates.add(cb.or(
                        getPredicates(query, cb, from, type,
                                ((OrExpression) expression).expressions).toArray(new Predicate[0])
                ));

            } else if (expression instanceof AndExpression) {
                predicates.add(cb.and(
                        getPredicates(query, cb, from, type,
                                ((AndExpression) expression).expressions).toArray(new Predicate[0])
                ));
            }
        }

        return predicates;
    }

    private static Attribute<?, ?> getAttribute(ManagedType<?> type, String field) {
        try {
            return type.getAttribute(field);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    String.format(
                            "Unable to locate attribute with the given name [%s] on this ManagedType [%s]," +
                                    " Are you sure this ManagedType or one of its ancestors contains such attribute?",
                            field,
                            type.getJavaType().getName()
                    )
            );
        }
    }

    private static Path<?> reuseOrCreateJoin(From<?, ?> from, Attribute<?, ?> attribute, String field, JoinType joinType) {
        return from.getJoins().stream()
                .filter(it -> it.getAttribute() == attribute)
                .findFirst()
                .orElseGet(() -> from.join(field, joinType));
    }

    @SuppressWarnings({"rawtypes"})
    private static ManagedType<?> extractSubFieldType(Attribute<?, ?> attribute) {
        return (ManagedType<?>) (attribute.isCollection() ? ((PluralAttribute) attribute).getElementType() :
                (((SingularAttribute) attribute).getType()));
    }

    private static String extractField(String field) {
        return field.contains(".") ? field.split("\\.")[0]
                .replaceAll("^[<>]+", "")   // remove '<' and '>' at start (left/right join indicators)
                .replaceAll("\\?$", "")     // remove '?' at end (left join indicators - optional chaining operator)
                : field;
    }

    private static SubField extractSubField(String field) {
        //if field is "abc.efg.xyz", then mainField=>"abc" and subField => "efg.xyz", so to support n-level association
        String mainField = Arrays.stream(field.split("\\.")).limit(1).collect(Collectors.joining("."));
        String subField = Arrays.stream(field.split("\\.")).skip(1).collect(Collectors.joining("."));

        JoinType joinType = mainField.startsWith("<") || mainField.endsWith("?") ? JoinType.LEFT // <abc or abc?
                : mainField.startsWith(">") ? JoinType.RIGHT // >abc
                : JoinType.INNER;   // abc
        return new SubField(subField, joinType);
    }

    private record SubField(String name, JoinType joinType) {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object convertValueToAttributeType(Object value, Class javaType) {
        if (value == null) {
            return null;
        }
        if (javaType.equals(LocalDate.class)) {
            return LocalDate.parse((CharSequence) value);
        }
        if (javaType.equals(Instant.class)) {
            return Instant.parse((CharSequence) value);
        }
        if (javaType.equals(LocalDateTime.class)) {
            return LocalDateTime.parse((CharSequence) value);
        }
        if (javaType.equals(OffsetDateTime.class)) {
            return OffsetDateTime.parse((CharSequence) value);
        }
        if (javaType.equals(ZonedDateTime.class)) {
            return ZonedDateTime.parse((CharSequence) value);
        }
        if (javaType.equals(HijrahDate.class)) {
            return DateTimeUtil.parseHijrah((String) value);
        }
        if (javaType.isEnum()) {
            if (Number.class.isAssignableFrom(value.getClass())) {
                return javaType.getEnumConstants()[((Number) value).intValue()];
            } else if (String.class.isAssignableFrom(value.getClass())) {
                return Enum.valueOf(javaType, (String) value);
            } else {
                return Enum.valueOf(javaType, ((Enum<?>) value).name());
            }
        }
        if (javaType.equals(UUID.class)) {
            return UUID.fromString((String) value);
        }

        // strings and numeric types don't need  conversion
        return value;
    }

    @SuppressWarnings({"rawtypes"})
    private static List<Object> convertValueToAttributeType(List<Object> values, Class javaType) {
        if (values == null || values.isEmpty() || values.get(0).getClass().equals(javaType)) {
            return values;
        }

        if (javaType.equals(Short.class)) {
            return values.stream().map(it -> ((Integer) it).shortValue()).collect(toList());
        }
        if (javaType.equals(Long.class)) {
            return values.stream().map(it -> ((Integer) it).longValue()).collect(toList());
        }
        if (javaType.equals(Byte.class)) {
            return values.stream().map(it -> ((Integer) it).byteValue()).collect(toList());
        }
        if (javaType.equals(Double.class)) {
            return values.stream().map(it -> ((Integer) it).doubleValue()).collect(toList());
        }
        if (javaType.equals(Float.class)) {
            return values.stream().map(it -> ((Integer) it).floatValue()).collect(toList());
        }

        return values.stream().map(it -> convertValueToAttributeType(it, javaType)).collect(toList());
    }
}
