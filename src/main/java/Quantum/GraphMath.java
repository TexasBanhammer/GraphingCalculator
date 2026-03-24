package Quantum;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.scene.paint.Color;

final class GraphMath {
    private GraphMath() {
    }

    static String formatIntersectionSummary(List<IntersectionPoint> intersections) {
        if (intersections.isEmpty()) {
            return "intersections: none in the current view";
        }

        StringBuilder builder = new StringBuilder("intersections: ");
        int shown = Math.min(3, intersections.size());
        for (int index = 0; index < shown; index++) {
            if (index > 0) {
                builder.append(" | ");
            }
            IntersectionPoint point = intersections.get(index);
            builder.append(point.pairLabel)
                    .append(" at (")
                    .append(formatNumber(point.x))
                    .append(", ")
                    .append(formatNumber(point.y))
                    .append(")");
        }

        if (intersections.size() > shown) {
            builder.append(" | +").append(intersections.size() - shown).append(" more");
        }
        return builder.toString();
    }

    static String formatIntersectionsForDialog(List<IntersectionPoint> intersections) {
        if (intersections.isEmpty()) {
            return "No intersections were found in the current graph window.";
        }

        StringBuilder builder = new StringBuilder();
        for (IntersectionPoint point : intersections) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(point.pairLabel)
                    .append(": (")
                    .append(formatNumber(point.x))
                    .append(", ")
                    .append(formatNumber(point.y))
                    .append(')');
        }
        return builder.toString();
    }

    static String formatNumber(double value) {
        double rounded = Math.abs(value) < 1e-9 ? 0.0 : value;
        if (Math.abs(rounded - Math.rint(rounded)) < 1e-9) {
            return Integer.toString((int) Math.rint(rounded));
        }
        return String.format(Locale.US, "%.3f", rounded);
    }

    static Color blend(Color first, Color second) {
        return Color.color(
                (first.getRed() + second.getRed()) / 2.0,
                (first.getGreen() + second.getGreen()) / 2.0,
                (first.getBlue() + second.getBlue()) / 2.0
        );
    }

    static List<IntersectionPoint> findIntersections(
            List<PlotExpression> expressions,
            double xMin,
            double xMax,
            double yMin,
            double yMax
    ) {
        if (expressions.size() < 2) {
            return List.of();
        }

        List<IntersectionPoint> points = new ArrayList<>();
        for (int firstIndex = 0; firstIndex < expressions.size(); firstIndex++) {
            for (int secondIndex = firstIndex + 1; secondIndex < expressions.size(); secondIndex++) {
                points.addAll(findIntersections(expressions.get(firstIndex), expressions.get(secondIndex), xMin, xMax, yMin, yMax));
            }
        }
        return List.copyOf(points);
    }

    private static List<IntersectionPoint> findIntersections(
            PlotExpression first,
            PlotExpression second,
            double xMin,
            double xMax,
            double yMin,
            double yMax
    ) {
        List<IntersectionPoint> points = new ArrayList<>();
        int samples = 1800;
        double step = (xMax - xMin) / samples;
        double previousX = xMin;
        double previousDifference = safeEvaluateDifference(first, second, previousX);

        maybeAddNearZeroIntersection(points, first, second, previousX, previousDifference, step, xMin, xMax, yMin, yMax);

        for (int index = 1; index <= samples; index++) {
            double currentX = xMin + index * step;
            double currentDifference = safeEvaluateDifference(first, second, currentX);

            if (isFinite(previousDifference) && isFinite(currentDifference)) {
                if (Math.abs(currentDifference) <= 1e-4) {
                    addIntersection(points, first, second, currentX, xMin, xMax, yMin, yMax);
                } else if ((previousDifference < 0.0 && currentDifference > 0.0)
                        || (previousDifference > 0.0 && currentDifference < 0.0)) {
                    addIntersection(
                            points,
                            first,
                            second,
                            refineIntersectionX(first, second, previousX, currentX),
                            xMin,
                            xMax,
                            yMin,
                            yMax
                    );
                } else {
                    maybeAddNearZeroIntersection(points, first, second, currentX, currentDifference, step, xMin, xMax, yMin, yMax);
                }
            }

            previousX = currentX;
            previousDifference = currentDifference;
        }

        return points;
    }

    private static void maybeAddNearZeroIntersection(
            List<IntersectionPoint> points,
            PlotExpression first,
            PlotExpression second,
            double x,
            double difference,
            double step,
            double xMin,
            double xMax,
            double yMin,
            double yMax
    ) {
        if (!isFinite(difference)) {
            return;
        }

        double threshold = Math.max(1e-4, (yMax - yMin) * 0.0025);
        if (Math.abs(difference) > threshold) {
            return;
        }

        double leftDifference = safeEvaluateDifference(first, second, x - step * 0.5);
        double rightDifference = safeEvaluateDifference(first, second, x + step * 0.5);
        if (isFinite(leftDifference)
                && isFinite(rightDifference)
                && Math.abs(difference) <= Math.abs(leftDifference)
                && Math.abs(difference) <= Math.abs(rightDifference)) {
            addIntersection(points, first, second, x, xMin, xMax, yMin, yMax);
        }
    }

    private static double refineIntersectionX(PlotExpression first, PlotExpression second, double left, double right) {
        double leftDifference = safeEvaluateDifference(first, second, left);

        for (int iteration = 0; iteration < 40; iteration++) {
            double midpoint = (left + right) / 2.0;
            double midpointDifference = safeEvaluateDifference(first, second, midpoint);
            if (!isFinite(midpointDifference) || Math.abs(midpointDifference) < 1e-7) {
                return midpoint;
            }

            if ((leftDifference < 0.0 && midpointDifference > 0.0)
                    || (leftDifference > 0.0 && midpointDifference < 0.0)) {
                right = midpoint;
            } else {
                left = midpoint;
                leftDifference = midpointDifference;
            }
        }

        return (left + right) / 2.0;
    }

    private static void addIntersection(
            List<IntersectionPoint> points,
            PlotExpression first,
            PlotExpression second,
            double candidateX,
            double xMin,
            double xMax,
            double yMin,
            double yMax
    ) {
        if (!isFinite(candidateX) || candidateX < xMin || candidateX > xMax) {
            return;
        }

        double firstY = safeEvaluate(first.expression, candidateX);
        double secondY = safeEvaluate(second.expression, candidateX);
        if (!isFinite(firstY) || !isFinite(secondY)) {
            return;
        }

        double y = (firstY + secondY) / 2.0;
        if (y < yMin || y > yMax) {
            return;
        }

        String pairLabel = first.label + " & " + second.label;
        for (IntersectionPoint point : points) {
            if (point.pairLabel.equals(pairLabel)
                    && Math.abs(point.x - candidateX) < Math.max(1e-3, (xMax - xMin) * 0.002)) {
                return;
            }
        }

        points.add(new IntersectionPoint(pairLabel, candidateX, y, blend(first.color, second.color)));
    }

    private static double safeEvaluateDifference(PlotExpression first, PlotExpression second, double x) {
        double firstValue = safeEvaluate(first.expression, x);
        double secondValue = safeEvaluate(second.expression, x);
        if (!isFinite(firstValue) || !isFinite(secondValue)) {
            return Double.NaN;
        }
        return firstValue - secondValue;
    }

    private static double safeEvaluate(ExpressionParser.Expression expression, double x) {
        try {
            return expression.evaluate(x);
        } catch (ArithmeticException exception) {
            return Double.NaN;
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
