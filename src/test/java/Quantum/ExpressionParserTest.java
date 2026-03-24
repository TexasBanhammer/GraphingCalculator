package Quantum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ExpressionParserTest {
    @Test
    void parsesArithmeticPrecedence() {
        ExpressionParser.Expression expression = new ExpressionParser("2 + 3 * x").parse();

        assertEquals(14.0, expression.evaluate(4.0), 1e-9);
    }

    @Test
    void parsesTrigAndConstants() {
        ExpressionParser.Expression expression = new ExpressionParser("sin(pi / 2) + cos(0)").parse();

        assertEquals(2.0, expression.evaluate(10.0), 1e-9);
    }

    @Test
    void supportsDerivativeOperation() {
        ExpressionParser.Expression expression = new ExpressionParser("diff(x^2)").parse();

        assertEquals(6.0, expression.evaluate(3.0), 1e-3);
    }

    @Test
    void supportsIntegralOperation() {
        ExpressionParser.Expression expression = new ExpressionParser("int(x)").parse();

        assertEquals(2.0, expression.evaluate(2.0), 1e-3);
    }

    @Test
    void rejectsUnknownFunctions() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ExpressionParser("foo(x)").parse()
        );

        assertEquals("Unknown function: foo", exception.getMessage());
    }
}
