package org.pod.ui;

import org.pod.analytics.AnalyticsReport;
import org.pod.app.AnalyticsApplicationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class AnalyticsDropFrame extends JFrame {
    private static final String DEFAULT_DROP_TEXT = "Arrastra y suelta un archivo .json aqui";

    private final AnalyticsApplicationService service;
    private final JLabel dropLabel;
    private final JLabel statusLabel;
    private final JTextArea reportArea;

    public AnalyticsDropFrame(AnalyticsApplicationService service) {
        this.service = service;

        setTitle("CityTyci Data Refinery - UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(850, 560));
        setLayout(new BorderLayout(10, 10));

        dropLabel = new JLabel(DEFAULT_DROP_TEXT, SwingConstants.CENTER);
        dropLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        dropLabel.setOpaque(true);
        dropLabel.setBackground(new Color(240, 244, 248));
        dropLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(72, 124, 188), 2),
                BorderFactory.createEmptyBorder(30, 20, 30, 20)));
        dropLabel.setTransferHandler(new JsonFileDropHandler());

        JButton chooseFileButton = new JButton("Seleccionar JSON");
        chooseFileButton.addActionListener(e -> openFilePicker());

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.add(dropLabel, BorderLayout.CENTER);
        topPanel.add(chooseFileButton, BorderLayout.EAST);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        statusLabel = new JLabel("Esperando archivo...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void openFilePicker() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Elegi un archivo JSON");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            processFile(chooser.getSelectedFile().toPath());
        }
    }

    private void processFile(Path filePath) {
        if (!filePath.toString().toLowerCase().endsWith(".json")) {
            showError("Solo se aceptan archivos .json");
            return;
        }

        dropLabel.setText("Procesando: " + filePath.getFileName());
        statusLabel.setText("Leyendo y analizando archivo...");

        new SwingWorker<AnalyticsReport, Void>() {
            @Override
            protected AnalyticsReport doInBackground() throws Exception {
                return service.analyze(filePath);
            }

            @Override
            protected void done() {
                try {
                    AnalyticsReport report = get();
                    reportArea.setText(report.toDisplayString());
                    statusLabel.setText("Analisis completado: " + filePath.getFileName());
                    dropLabel.setText(DEFAULT_DROP_TEXT);
                } catch (Exception ex) {
                    showError("No se pudo procesar el JSON: " + ex.getMessage());
                    dropLabel.setText(DEFAULT_DROP_TEXT);
                }
            }
        }.execute();
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class JsonFileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                List<?> droppedFiles = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (droppedFiles.isEmpty()) {
                    return false;
                }

                File firstFile = (File) droppedFiles.get(0);
                processFile(firstFile.toPath());
                return true;
            } catch (Exception e) {
                showError("No se pudo leer el archivo arrastrado.");
                return false;
            }
        }
    }
}

