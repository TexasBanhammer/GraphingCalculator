package Quantum;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

final class GraphView extends StackPane {
    private static final double DEFAULT_RANGE = 10.0;
    private static final double MIN_RANGE = 0.25;
    private static final double MAX_RANGE = 500.0;
    private static final double ZOOM_FACTOR = 0.8;
    private static final double LEFT_PADDING = 52.0;
    private static final double TOP_PADDING = 42.0;
    private static final double RIGHT_PADDING = 24.0;
    private static final double BOTTOM_PADDING = 46.0;

    private final Canvas canvas = new Canvas();
    private List<PlotExpression> expressions = List.of();
    private List<IntersectionPoint> intersections = List.of();
    private Consumer<List<IntersectionPoint>> intersectionListener;
    private double xMin = -DEFAULT_RANGE;
    private double xMax = DEFAULT_RANGE;
    private double yMin = -DEFAULT_RANGE;
    private double yMax = DEFAULT_RANGE;
    private double dragAnchorX;
    private double dragAnchorY;
    private double dragStartXMin;
    private double dragStartXMax;
    private double dragStartYMin;
    private double dragStartYMax;
    private boolean dragging;

    GraphView() {
        setMinWidth(400);
        setMinHeight(500);
        setPadding(new Insets(4));
        setStyle("""
                -fx-background-color: white;
                -fx-border-color: rgb(205,212,222);
                -fx-border-radius: 14;
                -fx-background-radius: 14;
                """);
        getChildren().add(canvas);

        widthProperty().addListener((obs, oldValue, newValue) -> resizeCanvas());
        heightProperty().addListener((obs, oldValue, newValue) -> resizeCanvas());
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> dragging = false);
        setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                zoomIn();
            } else if (event.getDeltaY() < 0) {
                zoomOut();
            }
        });
    }

    void setExpressions(List<String> texts) {
        List<PlotExpression> parsedExpressions = new ArrayList<>();
        for (int index = 0; index < texts.size(); index++) {
            String text = texts.get(index) == null ? "" : texts.get(index).trim();
            if (text.isEmpty()) {
                continue;
            }

            parsedExpressions.add(new PlotExpression(
                    "f" + (index + 1),
                    text,
                    AppTheme.PLOT_COLORS[index % AppTheme.PLOT_COLORS.length],
                    new ExpressionParser(text).parse()
            ));
        }

        expressions = List.copyOf(parsedExpressions);
        updateIntersections();
        draw();
    }

    void setIntersectionListener(Consumer<List<IntersectionPoint>> listener) {
        intersectionListener = listener;
        notifyIntersectionListener();
    }

    List<IntersectionPoint> getIntersections() {
        return List.copyOf(intersections);
    }

    void zoomIn() {
        applyZoom(ZOOM_FACTOR);
    }

    void zoomOut() {
        applyZoom(1.0 / ZOOM_FACTOR);
    }

    void resetView() {
        xMin = -DEFAULT_RANGE;
        xMax = DEFAULT_RANGE;
        yMin = -DEFAULT_RANGE;
        yMax = DEFAULT_RANGE;
        updateIntersections();
        draw();
    }

    private void resizeCanvas() {
        double width = Math.max(0, getWidth() - getInsets().getLeft() - getInsets().getRight());
        double height = Math.max(0, getHeight() - getInsets().getTop() - getInsets().getBottom());
        canvas.setWidth(width);
        canvas.setHeight(height);
        draw();
    }

    private void applyZoom(double factor) {
        double centerX = (xMin + xMax) / 2.0;
        double centerY = (yMin + yMax) / 2.0;
        double currentRange = (xMax - xMin) / 2.0;
        double nextRange = clamp(currentRange * factor, MIN_RANGE, MAX_RANGE);
        xMin = centerX - nextRange;
        xMax = centerX + nextRange;
        yMin = centerY - nextRange;
        yMax = centerY + nextRange;
        updateIntersections();
        draw();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        dragging = true;
        dragAnchorX = event.getX();
        dragAnchorY = event.getY();
        dragStartXMin = xMin;
        dragStartXMax = xMax;
        dragStartYMin = yMin;
        dragStartYMax = yMax;
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!dragging) {
            return;
        }

        double plotWidth = Math.max(1, canvas.getWidth() - LEFT_PADDING - RIGHT_PADDING);
        double plotHeight = Math.max(1, canvas.getHeight() - TOP_PADDING - BOTTOM_PADDING);
        double xRange = dragStartXMax - dragStartXMin;
        double yRange = dragStartYMax - dragStartYMin;
        double xShift = ((event.getX() - dragAnchorX) / plotWidth) * xRange;
        double yShift = ((event.getY() - dragAnchorY) / plotHeight) * yRange;

        xMin = dragStartXMin - xShift;
        xMax = dragStartXMax - xShift;
        yMin = dragStartYMin + yShift;
        yMax = dragStartYMax + yShift;
        updateIntersections();
        draw();
    }

    private void draw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        gc.setFill(new LinearGradient(
                0, 0, width, height, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(244, 247, 250)),
                new Stop(1, Color.rgb(225, 232, 242))
        ));
        gc.fillRoundRect(0, 0, width, height, 28, 28);

        double plotWidth = Math.max(1, width - LEFT_PADDING - RIGHT_PADDING);
        double plotHeight = Math.max(1, height - TOP_PADDING - BOTTOM_PADDING);

        drawGrid(gc, plotWidth, plotHeight);
        drawAxes(gc, plotWidth, plotHeight);
        drawLabels(gc, plotWidth, plotHeight);
        drawExpressions(gc, plotWidth, plotHeight);
        drawIntersections(gc, plotWidth, plotHeight);
        drawLegend(gc, plotWidth);
    }

    private void drawGrid(GraphicsContext gc, double plotWidth, double plotHeight) {
        gc.setStroke(Color.rgb(208, 216, 226));
        gc.setLineWidth(1);

        double tickStep = computeTickStep();
        double xStart = Math.ceil(xMin / tickStep) * tickStep;
        for (double value = xStart; value <= xMax + tickStep * 0.5; value += tickStep) {
            double x = mapX(value, plotWidth);
            gc.strokeLine(x, TOP_PADDING, x, TOP_PADDING + plotHeight);
        }

        double yStart = Math.ceil(yMin / tickStep) * tickStep;
        for (double value = yStart; value <= yMax + tickStep * 0.5; value += tickStep) {
            double y = mapY(value, plotHeight);
            gc.strokeLine(LEFT_PADDING, y, LEFT_PADDING + plotWidth, y);
        }
    }

    private void drawAxes(GraphicsContext gc, double plotWidth, double plotHeight) {
        gc.setStroke(Color.rgb(93, 109, 126));
        gc.setLineWidth(2);
        double zeroX = mapX(0.0, plotWidth);
        double zeroY = mapY(0.0, plotHeight);
        gc.strokeLine(LEFT_PADDING, zeroY, LEFT_PADDING + plotWidth, zeroY);
        gc.strokeLine(zeroX, TOP_PADDING, zeroX, TOP_PADDING + plotHeight);
    }

    private void drawLabels(GraphicsContext gc, double plotWidth, double plotHeight) {
        gc.setFill(Color.rgb(70, 78, 92));
        gc.setFont(Font.font("Segoe UI", 13));

        double tickStep = computeTickStep();
        double xStart = Math.ceil(xMin / tickStep) * tickStep;
        for (double value = xStart; value <= xMax + tickStep * 0.5; value += tickStep) {
            double x = mapX(value, plotWidth);
            gc.fillText(formatTickLabel(value), x - 10, TOP_PADDING + plotHeight + 20);
        }

        double yStart = Math.ceil(yMin / tickStep) * tickStep;
        for (double value = yStart; value <= yMax + tickStep * 0.5; value += tickStep) {
            if (Math.abs(value) < 1e-9) {
                continue;
            }
            double y = mapY(value, plotHeight);
            gc.fillText(formatTickLabel(value), LEFT_PADDING - 40, y + 4);
        }

        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        gc.fillText(formatGraphTitle(), LEFT_PADDING, 24);
    }

    private void drawExpressions(GraphicsContext gc, double plotWidth, double plotHeight) {
        for (PlotExpression expression : expressions) {
            gc.setStroke(expression.color);
            gc.setLineWidth(2.5);

            boolean hasPrevious = false;
            double previousX = 0;
            double previousY = 0;

            for (int pixel = 0; pixel < (int) plotWidth; pixel++) {
                double xValue = xMin + (pixel / plotWidth) * (xMax - xMin);
                double yValue = safeEvaluate(expression.expression, xValue);
                double yMargin = (yMax - yMin) * 3.0;

                if (!isFinite(yValue) || yValue < yMin - yMargin || yValue > yMax + yMargin) {
                    hasPrevious = false;
                    continue;
                }

                double screenX = mapX(xValue, plotWidth);
                double screenY = mapY(yValue, plotHeight);
                if (hasPrevious && Math.abs(screenY - previousY) < plotHeight) {
                    gc.strokeLine(previousX, previousY, screenX, screenY);
                }

                previousX = screenX;
                previousY = screenY;
                hasPrevious = true;
            }
        }
    }

    private void drawIntersections(GraphicsContext gc, double plotWidth, double plotHeight) {
        gc.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        for (IntersectionPoint point : intersections) {
            if (point.y < yMin || point.y > yMax) {
                continue;
            }

            double x = mapX(point.x, plotWidth);
            double y = mapY(point.y, plotHeight);
            gc.setFill(point.color);
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.fillText(point.pairLabel, x + 8, y - 8);
        }
    }

    private void drawLegend(GraphicsContext gc, double plotWidth) {
        if (expressions.isEmpty()) {
            return;
        }

        double legendX = LEFT_PADDING + plotWidth - 180;
        double legendY = TOP_PADDING + 18;
        gc.setFont(Font.font("Segoe UI", 13));

        for (int index = 0; index < expressions.size(); index++) {
            PlotExpression expression = expressions.get(index);
            double y = legendY + index * 18;
            gc.setStroke(expression.color);
            gc.setLineWidth(3);
            gc.strokeLine(legendX, y, legendX + 20, y);
            gc.setFill(Color.rgb(70, 78, 92));
            gc.fillText(expression.label + "(x)", legendX + 28, y + 4);
        }
    }

    private String formatGraphTitle() {
        if (expressions.isEmpty()) {
            return "Enter functions to graph";
        }

        StringBuilder builder = new StringBuilder("Graphs: ");
        int shown = Math.min(3, expressions.size());
        for (int index = 0; index < shown; index++) {
            if (index > 0) {
                builder.append("  ");
            }
            PlotExpression expression = expressions.get(index);
            builder.append(expression.label).append("(x) = ").append(expression.text);
        }
        if (expressions.size() > shown) {
            builder.append("  +").append(expressions.size() - shown).append(" more");
        }
        return builder.toString();
    }

    private double computeTickStep() {
        double range = xMax - xMin;
        double roughStep = range / 10.0;
        double magnitude = Math.pow(10, Math.floor(Math.log10(roughStep)));
        double normalized = roughStep / magnitude;

        if (normalized < 1.5) {
            return magnitude;
        }
        if (normalized < 3.0) {
            return 2.0 * magnitude;
        }
        if (normalized < 7.0) {
            return 5.0 * magnitude;
        }
        return 10.0 * magnitude;
    }

    private String formatTickLabel(double value) {
        double rounded = Math.abs(value) < 1e-9 ? 0.0 : value;
        if (Math.abs(rounded - Math.rint(rounded)) < 1e-9) {
            return Integer.toString((int) Math.rint(rounded));
        }
        return String.format(Locale.US, "%.2f", rounded);
    }

    private void updateIntersections() {
        intersections = GraphMath.findIntersections(expressions, xMin, xMax, yMin, yMax);
        notifyIntersectionListener();
    }

    private double safeEvaluate(ExpressionParser.Expression expression, double x) {
        try {
            return expression.evaluate(x);
        } catch (ArithmeticException exception) {
            return Double.NaN;
        }
    }

    private boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private void notifyIntersectionListener() {
        if (intersectionListener != null) {
            intersectionListener.accept(List.copyOf(intersections));
        }
    }

    private double mapX(double value, double plotWidth) {
        return LEFT_PADDING + ((value - xMin) / (xMax - xMin)) * plotWidth;
    }

    private double mapY(double value, double plotHeight) {
        return TOP_PADDING + ((yMax - value) / (yMax - yMin)) * plotHeight;
    }
}
