package org.pod.ui;

import org.pod.app.AnalyticsApplicationService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class UiMain {
    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default LAF if system LAF is unavailable.
        }

        SwingUtilities.invokeLater(() -> {
            AnalyticsDropFrame frame = new AnalyticsDropFrame(new AnalyticsApplicationService());
            frame.setVisible(true);
        });
    }

    static void main(String[] ignoredArgs) {
        launch();
    }
}

