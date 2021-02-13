package com.github.mhewedy.expressions;

import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static com.github.mhewedy.expressions.Expression.*;

class ExpressionsPredicateBuilder {

    static <T> Predicate getPredicate(Root<T> root, CriteriaBuilder cb, Expressions expressions) {

        Assert.notNull(expressions, "expressions must not be null!");

        List<Predicate> predicates = getPredicates(cb,
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
    static List<Predicate> getPredicates(CriteriaBuilder cb,
                                         Path<?> from, ManagedType<?> type,
                                         List<Expression> expressions) {

        List<Predicate> predicates = new ArrayList<>();

        for (Expression expression : expressions) {

            if (expression instanceof SingularExpression) {

                SingularExpression singularExpression = (SingularExpression) expression;

                SingularAttribute attribute = type.getSingularAttribute(singularExpression.field);
                Object attributeValue = convertValueToAttributeType(singularExpression.value, attribute.getJavaType());

                Path exprPath = from.get(attribute);

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
                            throw new IllegalArgumentException("field should be Number or Comparable: " +
                                    singularExpression);
                        }
                        break;
                    case $gte:
                        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.ge(exprPath, (Number) attributeValue);
                        } else if (Comparable.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.greaterThanOrEqualTo(exprPath, (Comparable) attributeValue);
                        } else {
                            throw new IllegalArgumentException("field should be Number or Comparable: " +
                                    singularExpression);
                        }
                        break;
                    case $lt:
                        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.lt(exprPath, (Number) attributeValue);
                        } else if (Comparable.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.lessThan(exprPath, (Comparable) attributeValue);
                        } else {
                            throw new IllegalArgumentException("field should be Number or Comparable: " +
                                    singularExpression);
                        }
                        break;
                    case $lte:
                        if (Number.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.le(exprPath, (Number) attributeValue);
                        } else if (Comparable.class.isAssignableFrom(attribute.getJavaType())) {
                            predicate = cb.lessThanOrEqualTo(exprPath, (Comparable) attributeValue);
                        } else {
                            throw new IllegalArgumentException("field should be Number or Comparable: " +
                                    singularExpression);
                        }
                        break;
                    // like
                    case $start:
                        predicate = cb.like(exprPath, attributeValue + "%");
                        break;
                    case $end:
                        predicate = cb.like(exprPath, "%" + attributeValue);
                        break;
                    case $contains:
                        predicate = cb.like(exprPath, "%" + attributeValue + "%");
                        break;
                    case $istart:
                        predicate = cb.like(cb.lower(exprPath), ((String) attributeValue).toLowerCase() + "%");
                        break;
                    case $iend:
                        predicate = cb.like(cb.lower(exprPath), "%" + ((String) attributeValue).toLowerCase());
                        break;
                    case $icontains:
                        predicate = cb.like(cb.lower(exprPath), "%" + ((String) attributeValue).toLowerCase() + "%");
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + singularExpression);
                }

                predicates.add(predicate);

            } else if (expression instanceof ListExpression) {

                ListExpression listExpression = (ListExpression) expression;

                SingularAttribute attribute = type.getSingularAttribute(listExpression.field);
                List<Object> attributeValues = listExpression.values;
                Path exprPath = from.get(attribute);

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
                        getPredicates(cb, from, type,
                                ((OrExpression) expression).expressions)
                                .toArray(new Predicate[0])
                ));

            } else if (expression instanceof AndExpression) {
                predicates.add(cb.and(
                        getPredicates(cb, from, type,
                                ((AndExpression) expression).expressions)
                                .toArray(new Predicate[0])
                ));
            }
        }

        return predicates;
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

        if (javaType.isEnum()) {
            if (Number.class.isAssignableFrom(value.getClass())) {
                return javaType.getEnumConstants()[((Number) value).intValue()];
            } else if (String.class.isAssignableFrom(value.getClass())) {
                return Enum.valueOf(javaType, (String) value);
            }
            throw new IllegalArgumentException("enum value should be number or string");
        }

        // strings and numeric types don't need  conversion
        return value;
    }
}
