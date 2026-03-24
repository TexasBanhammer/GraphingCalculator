package Quantum;

import javafx.scene.paint.Color;

final class PlotExpression {
    final String label;
    final String text;
    final Color color;
    final ExpressionParser.Expression expression;

    PlotExpression(String label, String text, Color color, ExpressionParser.Expression expression) {
        this.label = label;
        this.text = text;
        this.color = color;
        this.expression = expression;
    }
}
