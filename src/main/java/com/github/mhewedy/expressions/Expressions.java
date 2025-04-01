package com.github.mhewedy.expressions;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mhewedy.expressions.Expression.*;
import static com.github.mhewedy.expressions.Operator.$and;
import static com.github.mhewedy.expressions.Operator.$or;


/**
 * Represents Group of expression using mongodb query api.
 * <p>
 * Example:
 * <pre>
 * {
 *     "status": "A",
 *     "$or": [{ "qty": { "$lt": 30 } }, { "item": { "$in": ["A", "D"] } }]
 * }
 * </pre>
 * <p>
 * Support a list of Operators defined in {@link Operator}.
 *
 * @see ExpressionsRepository#findAll(Expressions)
 * @see ExpressionsRepository#findAll(Expressions, Sort)
 * @see ExpressionsRepository#findAll(Expressions, Pageable)
 * @see <a href="https://docs.mongodb.com/manual/tutorial/query-documents/">Mongo Query Documents</a>
 */
public class Expressions extends HashMap<String, Object> {

    /**
     * Create a new $or expression on the root,
     * then adds to it the current expressions attached at the root
     * and the expression passes as parameter.
     * <p>
     * Example:
     * Suppose we have the following expression as json: <pre> {"firstName": "ali"} </pre>
     * <p>
     * Then we added the following:
     * <pre>
     *  expressions.or(Expression.and(
     *          Expression.of("lastName", $eq, "ibrahim"),
     *          Expression.of("age", $gte, 10)
     *  ));
     * </pre>
     * Then the output could be represented as:
     *
     * <pre>
     * {
     *     "$or": [
     *          {"firstName": "ali"},
     *          {
     *              "$and": [
     *                  {"lastName": "ibrahim"},
     *                  {"age": {"$gte": 10}},
     *              ]
     *          }
     *     ]
     * }
     * </pre>
     * Or in sql as:
     * <pre>
     *  where firstName = "ali" or lastName = "ibrahim" and age &gt;= 10
     * </pre>
     */
    public Expressions or(Expression expression) {
        return append(expression, $or);
    }

    /**
     * Add the parameter expression to the list of expression in the expressions object.
     * <p>
     * Example:
     * Suppose we have the following expression as json:
     * <pre> {"firstName": "ali"} </pre>
     * <p>
     * Then we added the following:
     * <pre>
     *  expressions.and(Expression.of("birthDate", $gt, "1980-10-10"));
     *  expressions.and(Expression.or(
     *      Expression.of("lastName", $eq, "ibrahim"),
     *      Expression.of("age", $in, 10, 30)
     *  ));
     *
     * </pre>
     * Then the output could be represented as:
     *
     * <pre>
     * {
     *     "firstName": "ali",
     *     "birthDate": {"$gt": "1980-10-10"},
     *     "$or": [
     *          {"lastName": "ibrahim"},
     *          {"age": {"$in": [10, 30]}}
     *     ]
     * }
     * </pre>
     * Or in sql as:
     * <pre>
     * where firstName = "ali" and birthDate &gt; "1980-10-10"
     *     and (lastName = "ibrahim" or age in (10, 30) )
     * </pre>
     */
    public Expressions and(Expression expression) {
        return append(expression, $and);
    }

    private Expressions append(Expression expression, Operator operator) {
        Map<String, Object> tmp = new HashMap<>(this);
        this.clear();

        Map<String, Object> map = new HashMap<>();
        addToMap(expression, map);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        if (!tmp.isEmpty()) list.add(tmp);

        this.put(operator.name(), list);

        return this;
    }

    public <T> Specification<T> getSpecification() {
        return new ExpressionsRepositoryImpl.ExpressionsSpecification<>(this);
    }

    static Expressions of(Expression expression) {
        Expressions expressions = new Expressions();
        expressions.and(expression);
        return expressions;
    }

    /**
     * The bridge between {@link Expression} and the internal representation of mongodb query lang
     * represented by {@link Expressions} class.
     */
    private static void addToMap(Expression expression, Map<String, Object> map) {
        if (expression instanceof SingularExpression se) {
            if (se.operator == Operator.$eq) {
                map.put(se.field, se.value);
            } else {
                map.put(se.field, mapOf(se.operator.name(), se.value));
            }
        } else if (expression instanceof ListExpression le) {
            if (le.operator == Operator.$eq) {
                map.put(le.field, le.values);
            } else {
                map.put(le.field, mapOf(le.operator.name(), le.values));
            }
        } else if (expression instanceof OrExpression oe) {
            map.put($or.name(),
                    oe.expressions.stream().map(it -> {
                        Map<String, Object> m = new HashMap<>();
                        addToMap(it, m);
                        return m;
                    }).toList()
            );
        } else if (expression instanceof AndExpression ae) {
            map.put($and.name(),
                    ae.expressions.stream().map(it -> {
                        Map<String, Object> m = new HashMap<>();
                        addToMap(it, m);
                        return m;
                    }).toList()
            );
        }
    }

    List<Expression> getExpressions() {
        return getExpressions(this);
    }

    /**
     * Returns this object as list of {@link Expression} to be passed to
     * Spring Data Specification builder {@link ExpressionsPredicateBuilder}
     */
    @SuppressWarnings({"unchecked"})
    private static List<Expression> getExpressions(Map<String, Object> map) {

        List<Expression> expressions = new ArrayList<>();

        for (Entry<String, Object> entry : map.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            if ($or.name().equalsIgnoreCase(key)) {
                List<Map<String, Object>> valueList = (List<Map<String, Object>>) value;

                OrExpression orExpression = new OrExpression();
                expressions.add(orExpression);

                for (Map<String, Object> valueMap : valueList) {
                    orExpression.expressions.add(getExpressions(valueMap).get(0));
                }
            } else if ($and.name().equalsIgnoreCase(key)) {
                List<Map<String, Object>> valueList = (List<Map<String, Object>>) value;

                AndExpression andExpression = new AndExpression();
                expressions.add(andExpression);

                for (Map<String, Object> valueMap : valueList) {
                    andExpression.expressions.add(getExpressions(valueMap).get(0));
                }
            } else {
                if (value instanceof Map) { // value in the form of {"$operator": "value"}
                    Map<String, Object> valueMap = ((Map<String, Object>) value);
                    Entry<String, Object> first = valueMap.entrySet().iterator().next();

                    Operator operator = Operator.valueOf(first.getKey());

                    if (operator.isList) {
                        expressions.add(new ListExpression(key, operator, first.getValue()));
                    } else {
                        expressions.add(new SingularExpression(key, operator, first.getValue()));
                    }
                } else { // operator is "$eq"
                    expressions.add(new SingularExpression(key, Operator.$eq, value));
                }
            }
        }

        return expressions;
    }

    private static Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

    /**
     * Extracts all field names and their corresponding values from the current
     * {@code Expressions} object, including nested fields within `$and` and `$or`
     * compound operators.
     * <p>
     * This method traverses the structure of the {@code Expressions} object recursively.
     * If a compound operator (`$and` or `$or`) is encountered, it extracts fields and
     * values from all nested expressions.
     * </p>
     * <p>
     * Example:
     * Given the following {@code Expressions} structure:
     * <pre>
     * {
     *     "firstName": "John",
     *     "$or": [
     *         {"lastName": "Doe"},
     *         {"age": {"$gt": 30}}
     *     ]
     * }
     * </pre>
     * The resulting map of fields will be:
     * <pre>
     * {
     *     "firstName": "John",
     *     "lastName": "Doe",
     *     "age": 30
     * }
     * </pre>
     *
     * @return a map containing field names as keys and their corresponding values, including nested fields.
     */
    public Map<String, Object> extractFields() {
        return extractFields(getExpressions(this));
    }

    private static Map<String, Object> extractFields(List<Expression> expressionList) {
        var map = new HashMap<String, Object>();
        for (Expression expression : expressionList) {
            if (expression instanceof SingularExpression singularExpression) {
                map.put(singularExpression.field, singularExpression.value);
            } else if (expression instanceof ListExpression listExpression) {
                map.put(listExpression.field, listExpression.values);
            } else if (expression instanceof AndExpression andExpression) {
                map.putAll(extractFields(andExpression.expressions));
            } else if (expression instanceof OrExpression andExpression) {
                map.putAll(extractFields(andExpression.expressions));
            }
        }
        return map;
    }
}
