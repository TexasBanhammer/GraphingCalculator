package Quantum;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private static final Color[] PLOT_COLORS = {
            new Color(215, 65, 84),
            new Color(37, 99, 235),
            new Color(22, 163, 74),
            new Color(217, 119, 6),
            new Color(124, 58, 237),
            new Color(8, 145, 178)
    };

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUi);
    }

    private static void createAndShowUi() {
        applySystemLookAndFeel();

        JFrame frame = new JFrame("Graphing Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(16, 16));

        GraphPanel graphPanel = new GraphPanel();
        JPanel expressionsPanel = new JPanel();
        expressionsPanel.setLayout(new BoxLayout(expressionsPanel, BoxLayout.Y_AXIS));
        expressionsPanel.setOpaque(false);

        List<ExpressionRow> expressionRows = new ArrayList<>();
        JTextField[] activeField = new JTextField[1];

        addExpressionRow(expressionRows, expressionsPanel, graphPanel, activeField, "sin(x)");

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Functions");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        inputPanel.add(titleLabel, BorderLayout.NORTH);
        inputPanel.add(expressionsPanel, BorderLayout.CENTER);

        JLabel summaryLabel = new JLabel("intersections: plot at least two functions");
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        summaryLabel.setForeground(new Color(70, 78, 92));
        graphPanel.setIntersectionListener(points -> summaryLabel.setText(formatIntersectionSummary(points)));

        JButton graphButton = createActionButton("Graph");
        graphButton.addActionListener(event -> applyExpressions(graphPanel, expressionRows));

        JButton addFunctionButton = createSecondaryButton("Add Function");
        addFunctionButton.addActionListener(event -> addExpressionRow(expressionRows, expressionsPanel, graphPanel, activeField, ""));

        JButton clearButton = createSecondaryButton("Clear");
        clearButton.addActionListener(event -> {
            clearExpressionRows(expressionRows, expressionsPanel, activeField);
            applyExpressions(graphPanel, expressionRows);
        });

        JButton zoomInButton = createSecondaryButton("Zoom In");
        zoomInButton.addActionListener(event -> graphPanel.zoomIn());

        JButton zoomOutButton = createSecondaryButton("Zoom Out");
        zoomOutButton.addActionListener(event -> graphPanel.zoomOut());

        JButton intersectionsButton = createSecondaryButton("Intersections");
        intersectionsButton.addActionListener(event -> showIntersectionsDialog(graphPanel));

        JButton differentiateButton = createSecondaryButton("Differentiate");
        differentiateButton.addActionListener(event -> showDifferentiateDialog(frame, activeField, expressionRows));

        JButton integrateButton = createSecondaryButton("Integrate");
        integrateButton.addActionListener(event -> showIntegrateDialog(frame, activeField, expressionRows));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 18));
        topPanel.setOpaque(false);
        topPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new GridLayout(1, 8, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(graphButton);
        actionsPanel.add(addFunctionButton);
        actionsPanel.add(clearButton);
        actionsPanel.add(zoomInButton);
        actionsPanel.add(zoomOutButton);
        actionsPanel.add(intersectionsButton);
        actionsPanel.add(differentiateButton);
        actionsPanel.add(integrateButton);
        topPanel.add(actionsPanel, BorderLayout.EAST);
        topPanel.add(summaryLabel, BorderLayout.SOUTH);

        JPanel keypadPanel = createKeypad(activeField, expressionRows, graphPanel);
        keypadPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 18));

        applyExpressions(graphPanel, expressionRows);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(graphPanel, BorderLayout.CENTER);
        frame.add(keypadPanel, BorderLayout.EAST);
        frame.getContentPane().setBackground(new Color(232, 236, 242));

        frame.setMinimumSize(new Dimension(1280, 760));
        frame.setSize(1380, 860);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void addExpressionRow(
            List<ExpressionRow> expressionRows,
            JPanel expressionsPanel,
            GraphPanel graphPanel,
            JTextField[] activeField,
            String initialText
    ) {
        JTextField field = new JTextField(initialText);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(79, 93, 117), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel nameLabel = new JLabel();
        nameLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));

        JPanel colorChip = new JPanel();
        colorChip.setPreferredSize(new Dimension(16, 16));
        colorChip.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 1));

        JButton removeButton = createRowButton("Remove");

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.add(colorChip);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(removeButton);

        JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        rowPanel.add(nameLabel, BorderLayout.WEST);
        rowPanel.add(field, BorderLayout.CENTER);
        rowPanel.add(rightPanel, BorderLayout.EAST);
        rowPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        ExpressionRow row = new ExpressionRow(rowPanel, nameLabel, colorChip, field, removeButton);
        expressionRows.add(row);
        updateExpressionRowStyles(expressionRows);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                activeField[0] = field;
            }
        });
        field.addActionListener(event -> applyExpressions(graphPanel, expressionRows));

        removeButton.addActionListener(event -> {
            if (expressionRows.size() == 1) {
                field.setText("");
                activeField[0] = field;
                applyExpressions(graphPanel, expressionRows);
                return;
            }

            expressionRows.remove(row);
            expressionsPanel.remove(rowPanel);
            updateExpressionRowStyles(expressionRows);
            expressionsPanel.revalidate();
            expressionsPanel.repaint();

            if (activeField[0] == field && !expressionRows.isEmpty()) {
                activeField[0] = expressionRows.get(Math.max(0, expressionRows.size() - 1)).field;
            }

            applyExpressions(graphPanel, expressionRows);
        });

        expressionsPanel.add(rowPanel);
        expressionsPanel.revalidate();
        expressionsPanel.repaint();

        if (activeField[0] == null) {
            activeField[0] = field;
        }
    }

    private static void clearExpressionRows(
            List<ExpressionRow> expressionRows,
            JPanel expressionsPanel,
            JTextField[] activeField
    ) {
        while (expressionRows.size() > 1) {
            ExpressionRow row = expressionRows.remove(expressionRows.size() - 1);
            expressionsPanel.remove(row.panel);
        }

        if (!expressionRows.isEmpty()) {
            expressionRows.get(0).field.setText("");
            activeField[0] = expressionRows.get(0).field;
        }

        updateExpressionRowStyles(expressionRows);
        expressionsPanel.revalidate();
        expressionsPanel.repaint();
    }

    private static void updateExpressionRowStyles(List<ExpressionRow> expressionRows) {
        for (int index = 0; index < expressionRows.size(); index++) {
            ExpressionRow row = expressionRows.get(index);
            Color color = PLOT_COLORS[index % PLOT_COLORS.length];
            row.color = color;
            row.nameLabel.setText("f" + (index + 1) + "(x) =");
            row.nameLabel.setForeground(color);
            row.colorChip.setBackground(color);
        }
    }

    private static void applyExpressions(GraphPanel graphPanel, List<ExpressionRow> expressionRows) {
        List<String> expressions = new ArrayList<>();
        for (ExpressionRow row : expressionRows) {
            expressions.add(row.field.getText());
        }

        try {
            graphPanel.setExpressions(expressions);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    graphPanel,
                    exception.getMessage(),
                    "Invalid Expression",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String formatIntersectionSummary(List<IntersectionPoint> intersections) {
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

    private static String formatIntersectionsForDialog(List<IntersectionPoint> intersections) {
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

    private static String formatNumber(double value) {
        double rounded = Math.abs(value) < 1e-9 ? 0.0 : value;
        if (Math.abs(rounded - Math.rint(rounded)) < 1e-9) {
            return Integer.toString((int) Math.rint(rounded));
        }
        return String.format(Locale.US, "%.3f", rounded);
    }

    private static void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default look and feel.
        }
    }

    private static void showIntersectionsDialog(GraphPanel graphPanel) {
        JOptionPane.showMessageDialog(
                graphPanel,
                formatIntersectionsForDialog(graphPanel.getIntersections()),
                "Intersections",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static JPanel createKeypad(JTextField[] activeField, List<ExpressionRow> expressionRows, GraphPanel graphPanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JLabel keypadTitle = new JLabel("Keypad");
        keypadTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        keypadTitle.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel keypad = new JPanel(new GridBagLayout());
        keypad.setBackground(new Color(248, 250, 252));
        keypad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        keypad.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        List<List<String>> rows = List.of(
                List.of("7", "8", "9", "/"),
                List.of("4", "5", "6", "*"),
                List.of("1", "2", "3", "-"),
                List.of("0", ".", "x", "+"),
                List.of("(", ")", "^", "pi"),
                List.of("sin(", "cos(", "tan(", "sqrt("),
                List.of("log(", "ln(", "abs(", "C")
        );

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        for (int row = 0; row < rows.size(); row++) {
            List<String> keys = rows.get(row);
            for (int col = 0; col < keys.size(); col++) {
                String key = keys.get(col);
                constraints.gridx = col;
                constraints.gridy = row;
                JButton button = createKeyButton(key);
                button.addActionListener(event -> handleKeyPress(key, activeField, expressionRows, graphPanel));
                keypad.add(button, constraints);
            }
        }

        container.add(keypadTitle);
        container.add(Box.createVerticalStrut(8));
        container.add(keypad);

        return container;
    }

    private static void handleKeyPress(
            String key,
            JTextField[] activeField,
            List<ExpressionRow> expressionRows,
            GraphPanel graphPanel
    ) {
        JTextField targetField = resolveActiveField(activeField, expressionRows);
        if (targetField == null) {
            return;
        }

        if ("C".equals(key)) {
            targetField.setText("");
            applyExpressions(graphPanel, expressionRows);
            return;
        }

        String value = "pi".equals(key) ? "pi" : key;
        targetField.replaceSelection(value);
        targetField.requestFocusInWindow();
    }

    private static JTextField resolveActiveField(JTextField[] activeField, List<ExpressionRow> expressionRows) {
        if (activeField[0] != null) {
            return activeField[0];
        }
        if (expressionRows.isEmpty()) {
            return null;
        }
        activeField[0] = expressionRows.get(0).field;
        return activeField[0];
    }

    private static JButton createKeyButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        button.setBackground(new Color(255, 255, 255));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(213, 219, 229), 1));
        button.setPreferredSize(new Dimension(62, 44));
        button.setMargin(new Insets(4, 6, 4, 6));
        return button;
    }

    private static JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        button.setBackground(new Color(26, 95, 122));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        return button;
    }

    private static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBackground(new Color(228, 232, 238));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        return button;
    }

    private static JButton createRowButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(new Color(235, 238, 243));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return button;
    }

    private static Color blend(Color first, Color second) {
        return new Color(
                (first.getRed() + second.getRed()) / 2,
                (first.getGreen() + second.getGreen()) / 2,
                (first.getBlue() + second.getBlue()) / 2
        );
    }

    private static final class ExpressionRow {
        private final JPanel panel;
        private final JLabel nameLabel;
        private final JPanel colorChip;
        private final JTextField field;
        private final JButton removeButton;
        private Color color;

        private ExpressionRow(JPanel panel, JLabel nameLabel, JPanel colorChip, JTextField field, JButton removeButton) {
            this.panel = panel;
            this.nameLabel = nameLabel;
            this.colorChip = colorChip;
            this.field = field;
            this.removeButton = removeButton;
        }
    }

    private static final class PlotExpression {
        private final String label;
        private final String text;
        private final Color color;
        private final ExpressionParser.Expression expression;

        private PlotExpression(String label, String text, Color color, ExpressionParser.Expression expression) {
            this.label = label;
            this.text = text;
            this.color = color;
            this.expression = expression;
        }
    }

    private static final class IntersectionPoint {
        private final String pairLabel;
        private final double x;
        private final double y;
        private final Color color;

        private IntersectionPoint(String pairLabel, double x, double y, Color color) {
            this.pairLabel = pairLabel;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private static final class GraphPanel extends JPanel {
        private static final int GRID_SPACING = 40;
        private static final double DEFAULT_RANGE = 10.0;
        private static final double MIN_RANGE = 0.25;
        private static final double MAX_RANGE = 500.0;
        private static final double ZOOM_FACTOR = 0.8;

        private List<PlotExpression> expressions = List.of();
        private List<IntersectionPoint> intersections = List.of();
        private Consumer<List<IntersectionPoint>> intersectionListener;
        private double xMin = -DEFAULT_RANGE;
        private double xMax = DEFAULT_RANGE;
        private double yMin = -DEFAULT_RANGE;
        private double yMax = DEFAULT_RANGE;

        private GraphPanel() {
            setPreferredSize(new Dimension(820, 640));
            setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        }

        private void setExpressions(List<String> texts) {
            List<PlotExpression> parsedExpressions = new ArrayList<>();

            for (int index = 0; index < texts.size(); index++) {
                String text = texts.get(index) == null ? "" : texts.get(index).trim();
                if (text.isEmpty()) {
                    continue;
                }

                parsedExpressions.add(new PlotExpression(
                        "f" + (index + 1),
                        text,
                        PLOT_COLORS[index % PLOT_COLORS.length],
                        new ExpressionParser(text).parse()
                ));
            }

            expressions = List.copyOf(parsedExpressions);
            updateIntersections();
            repaint();
        }

        private void setIntersectionListener(Consumer<List<IntersectionPoint>> intersectionListener) {
            this.intersectionListener = intersectionListener;
            notifyIntersectionListener();
        }

        private List<IntersectionPoint> getIntersections() {
            return List.copyOf(intersections);
        }

        private void zoomIn() {
            applyZoom(ZOOM_FACTOR);
        }

        private void zoomOut() {
            applyZoom(1.0 / ZOOM_FACTOR);
        }

        private void applyZoom(double factor) {
            double currentRange = (xMax - xMin) / 2.0;
            double nextRange = clamp(currentRange * factor, MIN_RANGE, MAX_RANGE);
            xMin = -nextRange;
            xMax = nextRange;
            yMin = -nextRange;
            yMax = nextRange;
            updateIntersections();
            repaint();
        }

        private double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int width = getWidth();
            int height = getHeight();

            g2.setPaint(new GradientPaint(0, 0, new Color(244, 247, 250), width, height, new Color(225, 232, 242)));
            g2.fillRoundRect(0, 0, width, height, 30, 30);

            int left = 44;
            int top = 34;
            int plotWidth = width - left - 24;
            int plotHeight = height - top - 44;

            drawGrid(g2, left, top, plotWidth, plotHeight);
            drawAxes(g2, left, top, plotWidth, plotHeight);
            drawLabels(g2, left, top, plotWidth, plotHeight);
            drawExpressions(g2, left, top, plotWidth, plotHeight);
            drawIntersections(g2, left, top, plotWidth, plotHeight);
            drawLegend(g2, left, top, plotWidth);

            g2.dispose();
        }

        private void drawGrid(Graphics2D g2, int left, int top, int plotWidth, int plotHeight) {
            g2.setColor(new Color(208, 216, 226));
            for (int x = left; x <= left + plotWidth; x += GRID_SPACING) {
                g2.drawLine(x, top, x, top + plotHeight);
            }
            for (int y = top; y <= top + plotHeight; y += GRID_SPACING) {
                g2.drawLine(left, y, left + plotWidth, y);
            }
        }

        private void drawAxes(Graphics2D g2, int left, int top, int plotWidth, int plotHeight) {
            int zeroX = mapX(0.0, left, plotWidth);
            int zeroY = mapY(0.0, top, plotHeight);

            g2.setColor(new Color(93, 109, 126));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(left, zeroY, left + plotWidth, zeroY);
            g2.drawLine(zeroX, top, zeroX, top + plotHeight);
        }

        private void drawLabels(Graphics2D g2, int left, int top, int plotWidth, int plotHeight) {
            g2.setColor(new Color(70, 78, 92));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            double tickStep = computeTickStep();
            double xStart = Math.ceil(xMin / tickStep) * tickStep;
            double yStart = Math.ceil(yMin / tickStep) * tickStep;

            for (double value = xStart; value <= xMax + (tickStep * 0.5); value += tickStep) {
                int x = mapX(value, left, plotWidth);
                String label = formatTickLabel(value);
                g2.drawString(label, x - 10, top + plotHeight + 20);
            }

            for (double value = yStart; value <= yMax + (tickStep * 0.5); value += tickStep) {
                if (Math.abs(value) < 1e-9) {
                    continue;
                }

                int y = mapY(value, top, plotHeight);
                g2.drawString(formatTickLabel(value), left - 36, y + 4);
            }

            g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
            g2.drawString(formatGraphTitle(), left, 22);
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

        private void drawExpressions(Graphics2D g2, int left, int top, int plotWidth, int plotHeight) {
            for (PlotExpression expression : expressions) {
                g2.setColor(expression.color);
                g2.setStroke(new BasicStroke(2.5f));

                boolean hasPrevious = false;
                int previousX = 0;
                int previousY = 0;

                for (int pixel = 0; pixel < plotWidth; pixel++) {
                    double xValue = xMin + ((double) pixel / plotWidth) * (xMax - xMin);
                    double yValue = safeEvaluate(expression.expression, xValue);

                    if (!isFinite(yValue) || yValue < yMin * 4 || yValue > yMax * 4) {
                        hasPrevious = false;
                        continue;
                    }

                    int screenX = mapX(xValue, left, plotWidth);
                    int screenY = mapY(yValue, top, plotHeight);

                    if (hasPrevious && Math.abs(screenY - previousY) < plotHeight) {
                        g2.drawLine(previousX, previousY, screenX, screenY);
                    }

                    previousX = screenX;
                    previousY = screenY;
                    hasPrevious = true;
                }
            }
        }

        private void drawIntersections(Graphics2D g2, int left, int top, int plotWidth, int plotHeight) {
            if (intersections.isEmpty()) {
                return;
            }

            g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            for (IntersectionPoint point : intersections) {
                if (point.y < yMin || point.y > yMax) {
                    continue;
                }

                int x = mapX(point.x, left, plotWidth);
                int y = mapY(point.y, top, plotHeight);
                g2.setColor(point.color);
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.drawString(point.pairLabel, x + 8, y - 8);
            }
        }

        private void drawLegend(Graphics2D g2, int left, int top, int plotWidth) {
            if (expressions.isEmpty()) {
                return;
            }

            int legendX = left + plotWidth - 180;
            int legendY = top + 18;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            for (int index = 0; index < expressions.size(); index++) {
                PlotExpression expression = expressions.get(index);
                int y = legendY + (index * 18);
                g2.setColor(expression.color);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(legendX, y, legendX + 20, y);
                g2.setColor(new Color(70, 78, 92));
                g2.drawString(expression.label + "(x)", legendX + 28, y + 4);
            }
        }

        private void updateIntersections() {
            if (expressions.size() < 2) {
                intersections = List.of();
                notifyIntersectionListener();
                return;
            }

            List<IntersectionPoint> points = new ArrayList<>();
            for (int firstIndex = 0; firstIndex < expressions.size(); firstIndex++) {
                for (int secondIndex = firstIndex + 1; secondIndex < expressions.size(); secondIndex++) {
                    points.addAll(findIntersections(expressions.get(firstIndex), expressions.get(secondIndex)));
                }
            }

            intersections = List.copyOf(points);
            notifyIntersectionListener();
        }

        private List<IntersectionPoint> findIntersections(PlotExpression first, PlotExpression second) {
            List<IntersectionPoint> points = new ArrayList<>();
            int samples = 1800;
            double step = (xMax - xMin) / samples;
            double previousX = xMin;
            double previousDifference = safeEvaluateDifference(first, second, previousX);

            maybeAddNearZeroIntersection(points, first, second, previousX, previousDifference, step);

            for (int index = 1; index <= samples; index++) {
                double currentX = xMin + (index * step);
                double currentDifference = safeEvaluateDifference(first, second, currentX);

                if (isFinite(previousDifference) && isFinite(currentDifference)) {
                    if (Math.abs(currentDifference) <= 1e-4) {
                        addIntersection(points, first, second, currentX);
                    } else if ((previousDifference < 0.0 && currentDifference > 0.0)
                            || (previousDifference > 0.0 && currentDifference < 0.0)) {
                        addIntersection(points, first, second, refineIntersectionX(first, second, previousX, currentX));
                    } else {
                        maybeAddNearZeroIntersection(points, first, second, currentX, currentDifference, step);
                    }
                }

                previousX = currentX;
                previousDifference = currentDifference;
            }

            return points;
        }

        private void maybeAddNearZeroIntersection(
                List<IntersectionPoint> points,
                PlotExpression first,
                PlotExpression second,
                double x,
                double difference,
                double step
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
                addIntersection(points, first, second, x);
            }
        }

        private double refineIntersectionX(PlotExpression first, PlotExpression second, double left, double right) {
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

        private void addIntersection(List<IntersectionPoint> points, PlotExpression first, PlotExpression second, double candidateX) {
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

        private double safeEvaluateDifference(PlotExpression first, PlotExpression second, double x) {
            double firstValue = safeEvaluate(first.expression, x);
            double secondValue = safeEvaluate(second.expression, x);
            if (!isFinite(firstValue) || !isFinite(secondValue)) {
                return Double.NaN;
            }
            return firstValue - secondValue;
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

        private int mapX(double value, int left, int plotWidth) {
            return left + (int) Math.round(((value - xMin) / (xMax - xMin)) * plotWidth);
        }

        private int mapY(double value, int top, int plotHeight) {
            return top + (int) Math.round(((yMax - value) / (yMax - yMin)) * plotHeight);
        }
    }

    private static final class ExpressionParser {
        private final String input;
        private int position;

        private ExpressionParser(String input) {
            this.input = input.replace(" ", "").toLowerCase(Locale.ROOT);
        }

        private Expression parse() {
            Expression expression = parseExpression();
            if (position != input.length()) {
                throw new IllegalArgumentException("Unexpected token at position " + (position + 1));
            }
            return expression;
        }

        private Expression parseExpression() {
            Expression left = parseTerm();
            while (match('+') || match('-')) {
                char operator = input.charAt(position - 1);
                Expression right = parseTerm();
                Expression currentLeft = left;
                left = operator == '+'
                        ? x -> currentLeft.evaluate(x) + right.evaluate(x)
                        : x -> currentLeft.evaluate(x) - right.evaluate(x);
            }
            return left;
        }

        private Expression parseTerm() {
            Expression left = parsePower();
            while (match('*') || match('/')) {
                char operator = input.charAt(position - 1);
                Expression right = parsePower();
                Expression currentLeft = left;
                left = operator == '*'
                        ? x -> currentLeft.evaluate(x) * right.evaluate(x)
                        : x -> currentLeft.evaluate(x) / right.evaluate(x);
            }
            return left;
        }

        private Expression parsePower() {
            Expression base = parseUnary();
            if (match('^')) {
                Expression exponent = parsePower();
                return x -> Math.pow(base.evaluate(x), exponent.evaluate(x));
            }
            return base;
        }

        private Expression parseUnary() {
            if (match('+')) {
                return parseUnary();
            }
            if (match('-')) {
                Expression nested = parseUnary();
                return x -> -nested.evaluate(x);
            }
            return parsePrimary();
        }

        private Expression parsePrimary() {
            if (match('(')) {
                Expression expression = parseExpression();
                expect(')');
                return expression;
            }

            if (peekLetter()) {
                String identifier = parseIdentifier();
                if ("x".equals(identifier)) {
                    return x -> x;
                }
                if ("pi".equals(identifier)) {
                    return x -> Math.PI;
                }
                if ("e".equals(identifier)) {
                    return x -> Math.E;
                }

                expect('(');
                Expression argument = parseExpression();
                expect(')');
                return buildFunction(identifier, argument);
            }

            return parseNumber();
        }

        private Expression buildFunction(String name, Expression argument) {
            return switch (name) {
                case "sin" -> x -> Math.sin(argument.evaluate(x));
                case "cos" -> x -> Math.cos(argument.evaluate(x));
                case "tan" -> x -> Math.tan(argument.evaluate(x));
                case "sqrt" -> x -> Math.sqrt(argument.evaluate(x));
                case "log" -> x -> Math.log10(argument.evaluate(x));
                case "ln" -> x -> Math.log(argument.evaluate(x));
                case "abs" -> x -> Math.abs(argument.evaluate(x));
                default -> throw new IllegalArgumentException("Unknown function: " + name);
            };
        }

        private Expression parseNumber() {
            int start = position;
            while (position < input.length()
                    && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
                position++;
            }

            if (start == position) {
                throw new IllegalArgumentException("Expected number or expression at position " + (position + 1));
            }

            double value = Double.parseDouble(input.substring(start, position));
            return x -> value;
        }

        private String parseIdentifier() {
            int start = position;
            while (position < input.length() && Character.isLetter(input.charAt(position))) {
                position++;
            }
            return input.substring(start, position);
        }

        private void expect(char expected) {
            if (!match(expected)) {
                throw new IllegalArgumentException("Expected '" + expected + "' at position " + (position + 1));
            }
        }

        private boolean match(char expected) {
            if (position < input.length() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private boolean peekLetter() {
            return position < input.length() && Character.isLetter(input.charAt(position));
        }

        @FunctionalInterface
        private interface Expression {
            double evaluate(double x);
        }
    }
}
