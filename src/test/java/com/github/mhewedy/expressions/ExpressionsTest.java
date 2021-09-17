
package com.github.mhewedy.expressions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.mhewedy.expressions.Expression.*;
import static org.assertj.core.api.Assertions.assertThat;

class ExpressionsTest {

    @Test
    public void testObjectWithOrExpression() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        Expressions conditions =
                objectMapper.readValue(
                        "{ \"status\": \"A\",  \"$or\": [{ \"qty\": { \"$lt\": 30 } }, { \"item\": { \"$in\": [\"A\", \"D\"] } }] }",
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

}
