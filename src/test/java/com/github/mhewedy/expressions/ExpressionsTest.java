
package com.github.mhewedy.expressions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.mhewedy.expressions.Expression.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testObjectWithOrExpression() throws Exception {

        Expressions conditions =
                objectMapper.readValue("""
                                {
                                  "status": "A",
                                  "$or": [
                                    {
                                      "qty": { "$lt": 30 }
                                    },
                                    {
                                      "item": {"$in": ["A", "D"] }
                                    }
                                  ]
                                }
                                """,
                        Expressions.class);

        final List<Expression> expression = conditions.getExpressions();

        assertThat(expression).hasSize(2);

        assertThat(expression).hasAtLeastOneElementOfType(OrExpression.class);
        assertThat(expression).hasAtLeastOneElementOfType(SingularExpression.class);

        for (Expression element : expression) {
            if (element instanceof OrExpression) {
                OrExpression expr = (OrExpression) element;
                assertThat(expr.expressions).hasSize(2);

                assertThat(expr.expressions).hasAtLeastOneElementOfType(ListExpression.class);
                assertThat(expr.expressions).hasAtLeastOneElementOfType(SingularExpression.class);

                for (Expression ee : expr.expressions) {

                    if (ee instanceof SingularExpression) {

                        SingularExpression ees = (SingularExpression) ee;

                        assertThat(ees.field).isEqualTo("qty");
                        assertThat(ees.operator).isEqualTo(Operator.$lt);
                        assertThat(ees.value).isEqualTo(30);
                    }

                    if (ee instanceof ListExpression) {

                        ListExpression ees = (ListExpression) ee;

                        assertThat(ees.field).isEqualTo("item");
                        assertThat(ees.operator).isEqualTo(Operator.$in);
                        assertThat(ees.values).hasSize(2);
                        assertThat(ees.values).containsExactlyInAnyOrder("A", "D");
                    }
                }
            }

            if (element instanceof SingularExpression) {
                SingularExpression expr = (SingularExpression) element;
                assertThat(expr.field).isEqualTo("status");
                assertThat(expr.operator).isEqualTo(Operator.$eq);
                assertThat(expr.value).isEqualTo("A");
            }
        }
    }

    @Test
    void testExtractFieldsWithCompoundOperatorsJSON() throws Exception {
        String json = """
                {
                    "firstName": "John",
                    "$or": [
                        {
                            "lastName": "Doe"
                        },
                        {
                            "age": { "$gt": 30 }
                        }
                    ]
                }
                """;
        Expressions expressions = objectMapper.readValue(json, Expressions.class);
        Set<String> fields = expressions.extractFields().stream().map(Expressions.Field::name).collect(Collectors.toSet());

        assertEquals(Set.of("firstName", "lastName", "age"), fields);
    }

    @Test
    void testExtractFieldsWithEmptyJSON() throws Exception {
        String json = "{}";
        Expressions expressions = objectMapper.readValue(json, Expressions.class);
        Set<String> fields = expressions.extractFields().stream().map(Expressions.Field::name).collect(Collectors.toSet());

        assertTrue(fields.isEmpty());
    }

    @Test
    void testExtractFieldsWithNestedAndOperatorJSON() throws Exception {
        String json = """
                {
                    "$and": [
                        { "country": "USA" },
                        { "state": "California" }
                    ]
                }
                """;
        Expressions expressions = objectMapper.readValue(json, Expressions.class);
        Set<String> fields = expressions.extractFields().stream().map(Expressions.Field::name).collect(Collectors.toSet());

        assertEquals(Set.of("country", "state"), fields);
    }

    @Test
    void testExtractFieldsWithMultipleNestedOperatorsJSON() throws Exception {
        String json = """
                {
                    "$and": [
                        {
                            "$or": [
                                { "city": "New York" },
                                { "zipcode": "10001" }
                            ]
                        },
                        {
                            "city.country": "USA"
                        }
                    ]
                }
                """;
        Expressions expressions = objectMapper.readValue(json, Expressions.class);
        Set<String> fields = expressions.extractFields().stream().map(Expressions.Field::name).collect(Collectors.toSet());
        List<Object> values = expressions.extractFields().stream().map(Expressions.Field::value).toList();

        assertEquals(Set.of("city", "zipcode", "city.country"), fields);
        assertThatList(values).containsExactlyInAnyOrder("New York", "10001", "USA");
    }
}
