package Quantum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

class GraphMathTest {
    @Test
    void findsSingleIntersectionBetweenTwoLines() {
        PlotExpression first = new PlotExpression("f1", "x", Color.RED, new ExpressionParser("x").parse());
        PlotExpression second = new PlotExpression("f2", "2", Color.BLUE, new ExpressionParser("2").parse());

        List<IntersectionPoint> intersections = GraphMath.findIntersections(
                List.of(first, second),
                -10.0,
                10.0,
                -10.0,
                10.0
        );

        assertEquals(1, intersections.size());
        IntersectionPoint point = intersections.get(0);
        assertEquals("f1 & f2", point.pairLabel);
        assertEquals(2.0, point.x, 1e-3);
        assertEquals(2.0, point.y, 1e-3);
    }

    @Test
    void returnsNoIntersectionsWhenOnlyOneExpressionExists() {
        PlotExpression first = new PlotExpression("f1", "x", Color.RED, new ExpressionParser("x").parse());

        List<IntersectionPoint> intersections = GraphMath.findIntersections(
                List.of(first),
                -10.0,
                10.0,
                -10.0,
                10.0
        );

        assertTrue(intersections.isEmpty());
    }

    @Test
    void formatsIntersectionSummary() {
        List<IntersectionPoint> intersections = List.of(
                new IntersectionPoint("f1 & f2", 2.0, 2.0, Color.PURPLE)
        );

        assertEquals("intersections: f1 & f2 at (2, 2)", GraphMath.formatIntersectionSummary(intersections));
    }
}
