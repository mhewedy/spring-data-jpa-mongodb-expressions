package com.github.mhewedy.expressions;

import lombok.ToString;
import org.springframework.util.Assert;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Expression class considered the base for all other expressions as well as contains
 * factory method to build the expressions object. <br />
 * <p>
 * So it represents the java api for the expressions
 * <p>
 * <p>
 * Example: <br />
 * We can build complex expressions and pass it to the spring data jpa {@link ExpressionsRepository#findAll(Expressions)}
 * using the factory methods in this class as following: <br /><br />
 *
 * <pre>
 * var expressions = Expression.of("lastName", Operator.$eq, "ibrahim")
 *         .and(Expression.or(
 *                 Expression.of("age", Operator.$in, 10, 20),
 *                 Expression.of("birthDate", Operator.$lt, LocalDate.of(1980, 1, 1)))
 *         ).build();
 * </pre>
 * Then the output could be represented as:
 * <pre>
 * where lastName = "ibrahim" and (age in (10 , 20) or birth_date < "1980-01-01")
 * </pre>
 * <p>
 * The Expression API also service as an intermediate representations that resides between
 * the {@link Expressions} and the spring data JPA specifications
 * implementation {@link ExpressionsPredicateBuilder}.
 */
public abstract class Expression {

    public static Expression of(String field, Operator operator, String value) {
        return new SingularExpression(field, operator, value);
    }

    public static Expression of(String field, Operator operator, Number value) {
        return new SingularExpression(field, operator, value);
    }

    public static Expression of(String field, Operator operator, Temporal value) {
        return new SingularExpression(field, operator, value == null ? null : value.toString());
    }

    /**
     * Used with operators require list of elements,
     * such as {@link Operator#$in} and {@link Operator#$nin}
     */
    public static Expression of(String field, Operator operator, String... values) {
        return new ListExpression(field, operator, Arrays.asList(values));
    }

    /**
     * Used with operators require list of elements,
     * such as {@link Operator#$in} and {@link Operator#$nin}
     */
    public static Expression of(String field, Operator operator, Number... values) {
        return new ListExpression(field, operator, Arrays.asList(values));
    }

    /**
     * Used with operators require list of elements,
     * such as {@link Operator#$in} and {@link Operator#$nin}
     */
    public static Expression of(String field, Operator operator, Temporal... values) {
        return new ListExpression(field, operator,
                Arrays.stream(values).map(Object::toString).collect(toList())
        );
    }

    /**
     * factory method used to create new expression that "and" all input expressions.
     */
    public static Expression and(Expression... expressions) {
        final AndExpression andExpression = new AndExpression();
        Arrays.stream(expressions).forEach(andExpression::add);
        return andExpression;
    }

    /**
     * factory method used to create new expression that "or" all input expressions.
     */
    public static Expression or(Expression... expressions) {
        final OrExpression orExpression = new OrExpression();
        Arrays.stream(expressions).forEach(orExpression::add);
        return orExpression;
    }

    /**
     * apply "and" on current expression with the parameter expression.
     */
    public Expression and(Expression expression) {
        return new AndExpression()
                .add(this)
                .add(expression);
    }

    /**
     * apply "or" on current expression with the parameter expression.
     */
    public Expression or(Expression expression) {
        return new OrExpression()
                .add(this)
                .add(expression);
    }

    /**
     * Convert current object to {@link Expressions} to be used by the {@link ExpressionsRepository}.
     */
    public Expressions build() {
        return Expressions.of(this);
    }

    @ToString
    static class SingularExpression extends Expression {
        final String field;
        final Operator operator;
        final Object value;

        SingularExpression(String field, Operator operator, Object value) {
            Assert.notNull(field, "field must not be null!");
            Assert.notNull(operator, "operator must not be null!");

            this.field = field;
            this.operator = operator;
            this.value = value;

            if (operator.isList) {
                throw new IllegalArgumentException(
                        String.format("operator %s accepts list of values: [%s]", operator, this)
                );
            }
        }
    }

    @ToString
    static class ListExpression extends Expression {
        final String field;
        final Operator operator;
        final List<Object> values;

        @SuppressWarnings({"unchecked"})
        ListExpression(String field, Operator operator, Object values) {
            if (!(values instanceof List)) {
                throw new IllegalArgumentException(
                        String.format("operator %s accepts list of values: (field=%s, operator=%s, values=%s)",
                                operator, field, operator, values)
                );
            }
            List<Object> listValues = (List<Object>) values;
            Assert.notNull(field, "field must not be null!");
            Assert.notNull(operator, "operator must not be null!");
            Assert.notEmpty(listValues, "values should not be empty!");

            this.field = field;
            this.operator = operator;
            this.values = listValues;
        }

        ListExpression(String field, Operator operator, List<Object> values) {
            Assert.notNull(field, "field must not be null!");
            Assert.notNull(operator, "operator must not be null!");
            Assert.notEmpty(values, "values should not be empty!");

            this.field = field;
            this.operator = operator;
            this.values = values;

            if (!operator.isList) {
                throw new IllegalArgumentException(
                        String.format("operator %s doesn't accept list of values: [%s]", operator, this)
                );
            }
        }
    }

    @ToString
    static class OrExpression extends Expression {
        final List<Expression> expressions = new ArrayList<>();

        OrExpression add(Expression expression) {
            expressions.add(expression);
            return this;
        }
    }

    @ToString
    static class AndExpression extends Expression {
        final List<Expression> expressions = new ArrayList<>();

        AndExpression add(Expression expression) {
            expressions.add(expression);
            return this;
        }
    }
}
