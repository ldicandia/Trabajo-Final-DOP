package org.pod.ui;

import org.pod.analytics.AnalyticsReport;
import org.pod.app.AnalyticsApplicationService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.awt.Container;

public class AnalyticsDropFrame extends JFrame {
    private enum UiState {
        IDLE,
        DRAG_OVER,
        PROCESSING,
        SUCCESS,
        ERROR
    }

    private static final String DEFAULT_DROP_TEXT = "Arrastra y suelta un archivo .json";
    private static final Color BG_APP = new Color(245, 247, 252);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_NEUTRAL = new Color(220, 226, 236);
    private static final Color BORDER_PRIMARY = new Color(58, 123, 245);
    private static final Color TEXT_PRIMARY = new Color(32, 41, 62);
    private static final Color TEXT_SECONDARY = new Color(96, 108, 133);
    private static final Color STATUS_OK = new Color(18, 119, 73);
    private static final Color STATUS_ERROR = new Color(171, 34, 46);

    private final AnalyticsApplicationService service;
    private final JLabel dropLabel;
    private final JLabel hintLabel;
    private final JLabel statusLabel;
    private final JTextArea reportArea;
    private final JPanel dropCard;
    private final JProgressBar progressBar;
    private final JButton chooseFileButton;
    private final JButton clearButton;
    private final TransferHandler dropHandler;

    public AnalyticsDropFrame(AnalyticsApplicationService service) {
        this.service = service;

        setTitle("CityTyci Data Refinery");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setPreferredSize(new Dimension(980, 680));
        setLayout(new BorderLayout(12, 12));

        getContentPane().setBackground(BG_APP);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_APP);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 0, 22));

        JLabel titleLabel = new JLabel("CityTyci Data Refinery");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        JLabel subtitleLabel = new JLabel("Analiza eventos JSON con drag and drop o selector de archivo");
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JPanel titleBlock = new JPanel();
        titleBlock.setBackground(BG_APP);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBackground(BG_APP);
        body.setBorder(BorderFactory.createEmptyBorder(0, 22, 12, 22));

        dropHandler = new JsonFileDropHandler();

        dropCard = new JPanel();
        dropCard.setBackground(BG_CARD);
        dropCard.setLayout(new BoxLayout(dropCard, BoxLayout.Y_AXIS));
        dropCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_PRIMARY, 2, true),
                BorderFactory.createEmptyBorder(28, 26, 24, 26)
        ));

        dropLabel = new JLabel(DEFAULT_DROP_TEXT, SwingConstants.CENTER);
        dropLabel.setAlignmentX(CENTER_ALIGNMENT);
        dropLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        dropLabel.setForeground(TEXT_PRIMARY);
        dropLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        dropLabel.setTransferHandler(dropHandler);

        hintLabel = new JLabel("Formato esperado: arreglo de eventos (schema 1.0, 1.5, 2.0)", SwingConstants.CENTER);
        hintLabel.setAlignmentX(CENTER_ALIGNMENT);
        hintLabel.setForeground(TEXT_SECONDARY);
        hintLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        chooseFileButton = new JButton("Seleccionar JSON");
        chooseFileButton.setFocusPainted(false);
        chooseFileButton.setMargin(new Insets(8, 14, 8, 14));
        chooseFileButton.addActionListener(ignored -> openFilePicker());

        clearButton = new JButton("Limpiar");
        clearButton.setFocusPainted(false);
        clearButton.setMargin(new Insets(8, 14, 8, 14));
        clearButton.addActionListener(ignored -> clearReport());

        JPanel actions = new JPanel();
        actions.setBackground(BG_CARD);
        actions.add(chooseFileButton);
        actions.add(clearButton);

        dropCard.add(dropLabel);
        dropCard.add(Box.createVerticalStrut(8));
        dropCard.add(hintLabel);
        dropCard.add(Box.createVerticalStrut(16));
        dropCard.add(actions);
        registerDropTargets(dropCard);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        reportArea.setText("El reporte aparecera aqui despues de procesar un archivo JSON...");
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setBackground(BG_CARD);
        reportArea.setForeground(TEXT_PRIMARY);
        reportArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_NEUTRAL, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getViewport().setBackground(BG_CARD);

        JPanel reportCard = new JPanel(new BorderLayout());
        reportCard.setBackground(BG_CARD);
        reportCard.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        reportCard.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setBackground(BG_APP);
        centerPanel.add(dropCard, BorderLayout.NORTH);
        centerPanel.add(reportCard, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout(8, 8));
        statusBar.setBackground(BG_CARD);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_NEUTRAL, 1, true),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)
        ));
        statusLabel = new JLabel("Listo para procesar un archivo");
        statusLabel.setForeground(TEXT_SECONDARY);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setBorderPainted(false);

        statusBar.add(statusLabel, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        body.add(centerPanel, BorderLayout.CENTER);
        body.add(statusBar, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        setUiState(UiState.IDLE, "Listo para procesar un archivo");

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

    private void clearReport() {
        reportArea.setText("El reporte aparecera aqui despues de procesar un archivo JSON...");
        setUiState(UiState.IDLE, "Resultado limpiado. Arrastra otro archivo para continuar.");
    }

    private void processFile(Path filePath) {
        if (!filePath.toString().toLowerCase().endsWith(".json")) {
            showError("Solo se aceptan archivos .json");
            return;
        }

        setUiState(UiState.PROCESSING, "Leyendo y analizando " + filePath.getFileName() + "...");

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
                    setUiState(UiState.SUCCESS, "Analisis completado: " + filePath.getFileName());
                } catch (Exception ex) {
                    showError("No se pudo procesar el JSON: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showError(String message) {
        setUiState(UiState.ERROR, "Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setUiState(UiState state, String statusText) {
        statusLabel.setText(statusText);

        switch (state) {
            case IDLE -> {
                dropLabel.setText(DEFAULT_DROP_TEXT);
                hintLabel.setText("Formato esperado: arreglo de eventos (schema 1.0, 1.5, 2.0)");
                statusLabel.setForeground(TEXT_SECONDARY);
                dropCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_PRIMARY, 2, true),
                        BorderFactory.createEmptyBorder(28, 26, 24, 26)
                ));
                chooseFileButton.setEnabled(true);
                clearButton.setEnabled(true);
                progressBar.setVisible(false);
            }
            case DRAG_OVER -> {
                dropLabel.setText("Solta el archivo para procesarlo");
                hintLabel.setText("Se analizara automaticamente al soltar");
                statusLabel.setForeground(TEXT_SECONDARY);
                dropCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(38, 102, 225), 3, true),
                        BorderFactory.createEmptyBorder(27, 25, 23, 25)
                ));
            }
            case PROCESSING -> {
                dropLabel.setText("Procesando archivo...");
                hintLabel.setText("Esto puede tardar unos segundos si el JSON es muy grande");
                statusLabel.setForeground(TEXT_PRIMARY);
                chooseFileButton.setEnabled(false);
                clearButton.setEnabled(false);
                progressBar.setVisible(true);
            }
            case SUCCESS -> {
                dropLabel.setText(DEFAULT_DROP_TEXT);
                hintLabel.setText("Arrastra otro archivo para comparar resultados");
                statusLabel.setForeground(STATUS_OK);
                chooseFileButton.setEnabled(true);
                clearButton.setEnabled(true);
                progressBar.setVisible(false);
            }
            case ERROR -> {
                dropLabel.setText(DEFAULT_DROP_TEXT);
                hintLabel.setText("Revisa que el JSON tenga formato valido");
                statusLabel.setForeground(STATUS_ERROR);
                chooseFileButton.setEnabled(true);
                clearButton.setEnabled(true);
                progressBar.setVisible(false);
            }
        }
    }

    private void registerDropTargets(JComponent root) {
        root.setTransferHandler(dropHandler);
        registerDropTargetsRecursively(root);
    }

    private void registerDropTargetsRecursively(Container parent) {
        for (java.awt.Component child : parent.getComponents()) {
            if (child instanceof JComponent jComponent) {
                jComponent.setTransferHandler(dropHandler);
            }
            if (child instanceof Container container) {
                registerDropTargetsRecursively(container);
            }
        }
    }

    private class JsonFileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            boolean supported = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            if (supported && !progressBar.isVisible()) {
                setUiState(UiState.DRAG_OVER, "Solta el archivo para iniciar el analisis");
            }
            return supported;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                setUiState(UiState.IDLE, "Arrastre cancelado");
                return false;
            }

            try {
                List<?> droppedFiles = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (droppedFiles.isEmpty()) {
                    setUiState(UiState.IDLE, "No se detecto ningun archivo");
                    return false;
                }

                File firstFile = (File) droppedFiles.getFirst();
                processFile(firstFile.toPath());
                return true;
            } catch (Exception e) {
                showError("No se pudo leer el archivo arrastrado.");
                return false;
            }
        }
    }
}

