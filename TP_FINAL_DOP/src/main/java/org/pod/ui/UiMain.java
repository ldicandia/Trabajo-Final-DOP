package org.pod.ui;

import org.pod.app.AnalyticsApplicationService;

import javax.swing.SwingUtilities;

public class UiMain {
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            AnalyticsDropFrame frame = new AnalyticsDropFrame(new AnalyticsApplicationService());
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}

