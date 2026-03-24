package Quantum;

import javafx.scene.paint.Color;

final class IntersectionPoint {
    final String pairLabel;
    final double x;
    final double y;
    final Color color;

    IntersectionPoint(String pairLabel, double x, double y, Color color) {
        this.pairLabel = pairLabel;
        this.x = x;
        this.y = y;
        this.color = color;
    }
}
