package Quantum;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public final class GraphingCalculatorApp extends Application {
    private final List<ExpressionRow> expressionRows = new ArrayList<>();
    private final GraphView graphView = new GraphView();
    private final VBox expressionsBox = new VBox(10);
    private final Label summaryLabel = new Label("intersections: plot at least two functions");
    private TextField activeField;

    @Override
    public void start(Stage stage) {
        graphView.setIntersectionListener(points -> summaryLabel.setText(GraphMath.formatIntersectionSummary(points)));

        expressionsBox.setFillWidth(true);
        expressionsBox.setPadding(new Insets(2));
        addExpressionRow("sin(x)");

        VBox functionSection = createSection();
        functionSection.getChildren().addAll(
                createSectionTitle("Functions", "Build a set of equations and plot them together."),
                createExpressionScroller(),
                createFunctionButtons()
        );
        VBox.setVgrow(functionSection.getChildren().get(1), Priority.ALWAYS);

        VBox graphSection = createSection();
        graphSection.getChildren().addAll(createSectionTitle("Graph View", null), graphView);
        VBox.setVgrow(graphView, Priority.ALWAYS);

        VBox workspaceSection = createSection();
        workspaceSection.getChildren().addAll(
                createSectionTitle("Workspace", null),
                createWorkspacePanel(),
                createKeypad()
        );

        SplitPane splitPane = new SplitPane(functionSection, graphSection, workspaceSection);
        splitPane.setDividerPositions(0.24, 0.78);
        splitPane.setStyle("-fx-background-color: transparent;");

        BorderPane root = new BorderPane(splitPane);
        root.setPadding(new Insets(18));
        root.setBackground(new Background(new BackgroundFill(AppTheme.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root, 1440, 900, AppTheme.APP_BACKGROUND);
        stage.setTitle("Graphing Calculator");
        stage.setMinWidth(1200);
        stage.setMinHeight(760);
        stage.setScene(scene);
        stage.show();

        applyExpressions();
    }

    private VBox createSection() {
        VBox section = new VBox(16);
        section.setPadding(new Insets(18));
        section.setStyle(AppTheme.SECTION_STYLE);
        section.setFillWidth(true);
        return section;
    }

    private VBox createSectionTitle(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 22));

        VBox header = new VBox(6, titleLabel);
        if (subtitle != null && !subtitle.isBlank()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setFont(Font.font("Segoe UI", 14));
            subtitleLabel.setTextFill(javafx.scene.paint.Color.rgb(86, 95, 109));
            subtitleLabel.setWrapText(true);
            header.getChildren().add(subtitleLabel);
        }
        return header;
    }

    private ScrollPane createExpressionScroller() {
        ScrollPane scrollPane = new ScrollPane(expressionsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setBorder(new Border(new BorderStroke(
                AppTheme.PANEL_BORDER,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(1)
        )));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    private GridPane createFunctionButtons() {
        Button graphButton = createPrimaryButton("Graph");
        graphButton.setOnAction(event -> applyExpressions());

        Button addFunctionButton = createSecondaryButton("Add Function");
        addFunctionButton.setOnAction(event -> addExpressionRow(""));

        Button differentiateButton = createSecondaryButton("Differentiate");
        differentiateButton.setOnAction(event -> appendCalculatedExpression("diff"));

        Button integrateButton = createSecondaryButton("Integrate");
        integrateButton.setOnAction(event -> appendCalculatedExpression("int"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(graphButton, 0, 0);
        grid.add(addFunctionButton, 1, 0);
        grid.add(differentiateButton, 0, 1);
        grid.add(integrateButton, 1, 1);
        grid.getColumnConstraints().addAll(columnConstraint(), columnConstraint());
        return grid;
    }

    private VBox createWorkspacePanel() {
        summaryLabel.setFont(Font.font("Segoe UI", 14));
        summaryLabel.setWrapText(true);
        summaryLabel.setTextFill(javafx.scene.paint.Color.rgb(70, 78, 92));

        Button clearButton = createSecondaryButton("Clear");
        clearButton.setOnAction(event -> {
            clearExpressionRows();
            applyExpressions();
        });

        Button intersectionsButton = createSecondaryButton("Intersections");
        intersectionsButton.setOnAction(event -> showIntersectionsDialog());

        Button zoomInButton = createSecondaryButton("Zoom In");
        zoomInButton.setOnAction(event -> graphView.zoomIn());

        Button zoomOutButton = createSecondaryButton("Zoom Out");
        zoomOutButton.setOnAction(event -> graphView.zoomOut());

        Button resetZoomButton = createSecondaryButton("Reset Zoom");
        resetZoomButton.setOnAction(event -> graphView.resetView());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(clearButton, 0, 0);
        grid.add(intersectionsButton, 1, 0);
        grid.add(zoomInButton, 0, 1);
        grid.add(zoomOutButton, 1, 1);
        grid.add(resetZoomButton, 0, 2, 2, 1);
        grid.getColumnConstraints().addAll(columnConstraint(), columnConstraint());

        return new VBox(14, summaryLabel, grid);
    }

    private VBox createKeypad() {
        Label title = new Label("Keypad");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));

        List<List<String>> rows = List.of(
                List.of("7", "8", "9", "/"),
                List.of("4", "5", "6", "*"),
                List.of("1", "2", "3", "-"),
                List.of("0", ".", "x", "+"),
                List.of("(", ")", "^", "pi"),
                List.of("sin(", "cos(", "tan(", "sqrt("),
                List.of("log(", "ln(", "abs(", "C")
        );

        GridPane keypadGrid = new GridPane();
        keypadGrid.setHgap(8);
        keypadGrid.setVgap(8);
        keypadGrid.getColumnConstraints().addAll(
                columnConstraint(),
                columnConstraint(),
                columnConstraint(),
                columnConstraint()
        );

        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < rows.get(row).size(); column++) {
                String key = rows.get(row).get(column);
                Button button = createKeypadButton(key);
                button.setOnAction(event -> handleKeyPress(key));
                keypadGrid.add(button, column, row);
            }
        }

        VBox keypadBox = new VBox(12, title, keypadGrid);
        keypadBox.setPadding(new Insets(16));
        keypadBox.setStyle("""
                -fx-background-color: white;
                -fx-border-color: rgb(217,223,230);
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);
        return keypadBox;
    }

    private void addExpressionRow(String initialText) {
        TextField field = new TextField(initialText);
        field.setFont(Font.font("Segoe UI", 18));
        field.setPrefHeight(40);
        field.setOnMouseClicked(event -> activeField = field);
        field.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                activeField = field;
            }
        });
        field.setOnAction(event -> applyExpressions());
        HBox.setHgrow(field, Priority.ALWAYS);

        Label nameLabel = new Label();
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));

        Rectangle colorChip = new Rectangle(16, 16);
        colorChip.setArcWidth(6);
        colorChip.setArcHeight(6);

        Button removeButton = createRowButton("Remove");
        HBox header = new HBox(10, nameLabel, spacer(), colorChip, removeButton);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, header, field);
        card.setPadding(new Insets(12));
        card.setFillWidth(true);
        card.setStyle("""
                -fx-background-color: white;
                -fx-border-color: rgb(217,223,230);
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);
        card.setMaxWidth(Double.MAX_VALUE);

        ExpressionRow row = new ExpressionRow(card, nameLabel, colorChip, field);
        expressionRows.add(row);
        updateExpressionRowStyles();

        removeButton.setOnAction(event -> {
            if (expressionRows.size() == 1) {
                field.clear();
                activeField = field;
                applyExpressions();
                return;
            }

            expressionRows.remove(row);
            expressionsBox.getChildren().remove(card);
            updateExpressionRowStyles();

            if (activeField == field && !expressionRows.isEmpty()) {
                activeField = expressionRows.get(Math.max(0, expressionRows.size() - 1)).field;
            }

            applyExpressions();
        });

        expressionsBox.getChildren().add(card);
        if (activeField == null) {
            activeField = field;
        }
    }

    private void clearExpressionRows() {
        while (expressionRows.size() > 1) {
            ExpressionRow row = expressionRows.remove(expressionRows.size() - 1);
            expressionsBox.getChildren().remove(row.card);
        }

        if (!expressionRows.isEmpty()) {
            expressionRows.get(0).field.clear();
            activeField = expressionRows.get(0).field;
        }

        updateExpressionRowStyles();
    }

    private void updateExpressionRowStyles() {
        for (int index = 0; index < expressionRows.size(); index++) {
            ExpressionRow row = expressionRows.get(index);
            var color = AppTheme.PLOT_COLORS[index % AppTheme.PLOT_COLORS.length];
            row.nameLabel.setText("f" + (index + 1) + "(x) =");
            row.nameLabel.setTextFill(color);
            row.colorChip.setFill(color);
        }
    }

    private void applyExpressions() {
        List<String> expressions = new ArrayList<>();
        for (ExpressionRow row : expressionRows) {
            expressions.add(row.field.getText());
        }

        try {
            graphView.setExpressions(expressions);
        } catch (IllegalArgumentException exception) {
            showAlert(Alert.AlertType.ERROR, "Invalid Expression", exception.getMessage());
        }
    }

    private void appendCalculatedExpression(String operation) {
        ActiveExpression activeExpression = getActiveExpression();
        if (activeExpression == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Expression", "Select a function row with an expression first.");
            return;
        }

        addExpressionRow(operation + "(" + activeExpression.text + ")");
        ExpressionRow newRow = expressionRows.get(expressionRows.size() - 1);
        activeField = newRow.field;
        newRow.field.requestFocus();
        newRow.field.positionCaret(newRow.field.getText().length());
        applyExpressions();
    }

    private ActiveExpression getActiveExpression() {
        TextField field = resolveActiveField();
        if (field == null) {
            return null;
        }

        String text = field.getText() == null ? "" : field.getText().trim();
        if (text.isEmpty()) {
            return null;
        }

        for (int index = 0; index < expressionRows.size(); index++) {
            if (expressionRows.get(index).field == field) {
                return new ActiveExpression(text);
            }
        }

        return null;
    }

    private TextField resolveActiveField() {
        if (activeField != null) {
            return activeField;
        }
        if (expressionRows.isEmpty()) {
            return null;
        }
        activeField = expressionRows.get(0).field;
        return activeField;
    }

    private void handleKeyPress(String key) {
        TextField targetField = resolveActiveField();
        if (targetField == null) {
            return;
        }

        if ("C".equals(key)) {
            targetField.clear();
            applyExpressions();
            return;
        }

        targetField.insertText(targetField.getCaretPosition(), "pi".equals(key) ? "pi" : key);
        targetField.requestFocus();
    }

    private void showIntersectionsDialog() {
        showAlert(Alert.AlertType.INFORMATION, "Intersections", GraphMath.formatIntersectionsForDialog(graphView.getIntersections()));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(40);
        button.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        button.setStyle("""
                -fx-background-color: rgb(26,95,122);
                -fx-text-fill: white;
                -fx-background-radius: 8;
                """);
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(40);
        button.setFont(Font.font("Segoe UI", 14));
        button.setStyle("""
                -fx-background-color: rgb(228,232,238);
                -fx-text-fill: rgb(33,37,41);
                -fx-background-radius: 8;
                """);
        return button;
    }

    private Button createRowButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", 12));
        button.setStyle("""
                -fx-background-color: rgb(235,238,243);
                -fx-background-radius: 8;
                """);
        return button;
    }

    private Button createKeypadButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(36);
        button.setFont(Font.font("Segoe UI", 14));
        button.setStyle("""
                -fx-background-color: rgb(255,255,255);
                -fx-border-color: rgb(213,219,229);
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                """);
        return button;
    }

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private static ColumnConstraints columnConstraint() {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(50);
        constraints.setFillWidth(true);
        constraints.setHgrow(Priority.ALWAYS);
        return constraints;
    }

    private static final class ExpressionRow {
        private final VBox card;
        private final Label nameLabel;
        private final Rectangle colorChip;
        private final TextField field;

        private ExpressionRow(VBox card, Label nameLabel, Rectangle colorChip, TextField field) {
            this.card = card;
            this.nameLabel = nameLabel;
            this.colorChip = colorChip;
            this.field = field;
        }
    }

    private static final class ActiveExpression {
        private final String text;

        private ActiveExpression(String text) {
            this.text = text;
        }
    }
}
