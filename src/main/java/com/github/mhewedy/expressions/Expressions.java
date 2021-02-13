package com.github.mhewedy.expressions;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mhewedy.expressions.Expression.*;


/**
 * Represents Group of expression using mongodb query api <br />
 * <p>
 * Example:
 * <pre>
 * {
 *     "status": "A",
 *     "$or": [{ "qty": { "$lt": 30 } }, { "item": { "$in": ["A", "D"] } }]
 * }
 * </pre>
 * <p>
 * Support a list of Operators defined in {@link Operator}. <br />
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
     * Example: <br />
     * Suppose we have the following expression as json: <pre> {"firstName": "ali"} </pre>
     * <p>
     * Then we added the following:
     * <pre>
     *  expressions.or(Expression.and(
     *          Expression.of("lastName", Operator.$eq, "ibrahim"),
     *          Expression.of("age", Operator.$gte, 10)
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
     *  where firstName = "ali" or lastName = "ibrahim" and age >= 10
     * </pre>
     */
    public Expressions or(Expression expression) {

        Map<String, Object> tmp = new HashMap<>(this);
        this.clear();

        List<Map<String, Object>> list = new ArrayList<>();
        this.put(Operator.$or.name(), list);

        list.add(tmp);

        Map<String, Object> map = new HashMap<>();
        list.add(map);
        add(expression, map);

        return this;
    }

    /**
     * Add the parameter expression to the list of expression in the expressions object.
     * <p>
     * Example: <br />
     * Suppose we have the following expression as json:
     * <pre> {"firstName": "ali"} </pre>
     * <p>
     * Then we added the following:
     * <pre>
     *  expressions.and(Expression.of("birthDate", Operator.$gt, "1980-10-10"));
     *  expressions.and(Expression.or(
     *      Expression.of("lastName", Operator.$eq, "ibrahim"),
     *      Expression.of("age", Operator.$in, 10, 30)
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
     * where firstName = "ali" and birthDate > "1980-10-10"
     *     and (lastName = "ibrahim" or age in (10, 30) )
     * </pre>
     */
    public Expressions and(Expression expression) {
        add(expression, this);
        return this;
    }

    static Expressions of(Expression expression) {
        Expressions expressions = new Expressions();
        expressions.and(expression);
        return expressions;
    }

    /**
     * The bridge between {@link Expression} and the internal representation of mongodb query lang
     * represented by {@link Expressions} class. <br />
     */
    private static void add(Expression expression, Map<String, Object> map) {

        if (expression instanceof SingularExpression) {
            SingularExpression se = (SingularExpression) expression;

            if (se.operator == Operator.$eq) {
                map.put(se.field, se.value);
            } else {
                map.put(se.field, map(se.operator.name(), se.value));
            }
        } else if (expression instanceof ListExpression) {
            ListExpression le = (ListExpression) expression;

            if (le.operator == Operator.$eq) {
                map.put(le.field, le.values);
            } else {
                map.put(le.field, map(le.operator.name(), le.values));
            }
        } else if (expression instanceof OrExpression) {
            OrExpression oe = (OrExpression) expression;

            List<Map<String, Object>> list = new ArrayList<>();
            map.put(Operator.$or.name(), list);

            oe.expressions.forEach(it -> {
                Map<String, Object> m = new HashMap<>();
                list.add(m);
                add(it, m);
            });
        } else if (expression instanceof AndExpression) {
            AndExpression ae = (AndExpression) expression;

            List<Map<String, Object>> list = new ArrayList<>();
            map.put(Operator.$and.name(), list);

            ae.expressions.forEach(it -> {
                Map<String, Object> m = new HashMap<>();
                list.add(m);
                add(it, m);
            });
        }
    }

    List<Expression> getExpressions() {
        return getExpressions(this);
    }

    /**
     * Returns this object as as list of {@link Expression} to be passed to
     * Spring Data Specification builder {@link ExpressionsPredicateBuilder}
     */
    @SuppressWarnings({"unchecked"})
    private List<Expression> getExpressions(Map<String, Object> map) {

        List<Expression> expressions = new ArrayList<>();

        for (Entry<String, Object> entry : map.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            if (Operator.$or.name().equalsIgnoreCase(key)) {
                List<Map<String, Object>> valueList = (List<Map<String, Object>>) value;

                OrExpression orExpression = new OrExpression();
                expressions.add(orExpression);

                for (Map<String, Object> valueMap : valueList) {
                    orExpression.expressions.add(getExpressions(valueMap).get(0));
                }
            } else if (Operator.$and.name().equalsIgnoreCase(key)) {
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

    private static Map<String, Object> map(String key, Object value) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, value);
        return m;
    }

}
