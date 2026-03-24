package Quantum;

import javafx.scene.paint.Color;

final class AppTheme {
    static final Color[] PLOT_COLORS = {
            Color.rgb(215, 65, 84),
            Color.rgb(37, 99, 235),
            Color.rgb(22, 163, 74),
            Color.rgb(217, 119, 6),
            Color.rgb(124, 58, 237),
            Color.rgb(8, 145, 178)
    };
    static final Color APP_BACKGROUND = Color.rgb(232, 236, 242);
    static final Color PANEL_BORDER = Color.rgb(205, 212, 222);
    static final String SECTION_STYLE = """
            -fx-background-color: rgb(248,250,252);
            -fx-border-color: rgb(205,212,222);
            -fx-border-radius: 12;
            -fx-background-radius: 12;
            """;

    private AppTheme() {
    }
}
