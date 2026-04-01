package org.pod;

import org.pod.app.AnalyticsApplicationService;
import org.pod.analytics.AnalyticsReport;
import org.pod.ui.UiMain;

import java.io.File;

public class Main {
    static void main(String[] args) throws Exception {
        if (args.length == 0 || "--ui".equalsIgnoreCase(args[0])) {
            UiMain.launch();
            return;
        }

        File inputFile = resolveInputFile(args);
        if (inputFile == null) {
            System.out.println("No se encontro el archivo JSON. Pasa una ruta por argumento o usa --ui para abrir la interfaz.");
            return;
        }

        AnalyticsApplicationService service = new AnalyticsApplicationService();
        AnalyticsReport report = service.analyze(inputFile.toPath());
        report.printReport();
    }

    private static File resolveInputFile(String[] args) {
        if (args.length > 0 && !"--ui".equalsIgnoreCase(args[0])) {
            File customInput = new File(args[0]);
            return customInput.exists() ? customInput : null;
        }

        File inputFile = new File("example_input.json");
        if (inputFile.exists()) {
            return inputFile;
        }

        inputFile = new File("TP_FINAL_DOP/example_input.json");
        return inputFile.exists() ? inputFile : null;
    }
}
