package sim;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// LogicSimIde class to launch the built-in IDE for the simulation
// Author: Brandon Quandt 
public final class LogicSimIde {
    // Colors for the UI
    // TODO: Make these more customizable for the user, maybe switch to another UI library like JavaFX or SwingX or port code to another language with a more modern UI library 
    private static final Color BG = new Color(26, 28, 34);
    private static final Color PANEL_BG = new Color(34, 37, 45);
    private static final Color INPUT_BG = new Color(20, 22, 28);
    private static final Color FG = new Color(229, 233, 240);
    private static final Color BORDER = new Color(70, 75, 89);

    private LogicSimIde() {
    }

    
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();
            JFrame frame = new JFrame("Digital Logic Sim IDE");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(buildContent(frame));
            frame.setSize(new Dimension(1100, 760));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // TODO: Split up this method into smaller methods for better readability   
    private static JPanel buildContent(JFrame frame) {
        JTextArea netlistEditor = makeEditor();
        JTextArea stimulusEditor = makeEditor();
        VcdWaveformPanel waveformPanel = new VcdWaveformPanel();
        final String[] latestVcd = new String[] {""};

        JTextField maxTimeField = new JTextField("100");
        JLabel statusLabel = new JLabel("Ready");

        JButton loadNetlistBtn = new JButton("Open .v");
        loadNetlistBtn.addActionListener(e -> openFileInto(frame, netlistEditor));

        JButton loadStimulusBtn = new JButton("Open .stim");
        loadStimulusBtn.addActionListener(e -> openFileInto(frame, stimulusEditor));

        JButton saveVcdBtn = new JButton("Save VCD As...");
        saveVcdBtn.addActionListener(e -> saveTextToFile(frame, latestVcd[0]));

        JButton runBtn = new JButton("Run");
        runBtn.addActionListener(
                e -> runSimulation(netlistEditor, stimulusEditor, waveformPanel, latestVcd, maxTimeField, statusLabel));
        runBtn.setBackground(new Color(67, 117, 212));
        runBtn.setForeground(Color.WHITE);

        JPanel controls = new JPanel(new GridLayout(2, 1, 6, 6));
        JPanel topRow = new JPanel(new BorderLayout(6, 6));
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.add(loadNetlistBtn);
        leftActions.add(loadStimulusBtn);
        leftActions.add(saveVcdBtn);
        topRow.add(leftActions, BorderLayout.WEST);

        JLabel zoomLabel = new JLabel("Zoom: 100%");
        JButton zoomOutBtn = new JButton("-");
        JButton zoomInBtn = new JButton("+");
        JButton zoomResetBtn = new JButton("1:1");
        JButton zoomFitBtn = new JButton("Fit");

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.add(new JLabel("Waveform"));
        rightActions.add(zoomOutBtn);
        rightActions.add(zoomInBtn);
        rightActions.add(zoomResetBtn);
        rightActions.add(zoomFitBtn);
        rightActions.add(zoomLabel);
        rightActions.add(runBtn);
        topRow.add(rightActions, BorderLayout.EAST);

        JPanel bottomRow = new JPanel(new BorderLayout(6, 6));
        bottomRow.add(new JLabel("Max Time (ns):"), BorderLayout.WEST);
        bottomRow.add(maxTimeField, BorderLayout.CENTER);
        bottomRow.add(statusLabel, BorderLayout.EAST);

        controls.add(topRow);
        controls.add(bottomRow);

        JScrollPane netlistScroll = wrap("Netlist (.v)", netlistEditor);
        JScrollPane stimScroll = wrap("Stimulus (.stim)", stimulusEditor);
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, netlistScroll, stimScroll);
        topSplit.setResizeWeight(0.5);

        JScrollPane vcdScroll = wrap("VCD Waveform", waveformPanel);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, vcdScroll);
        mainSplit.setResizeWeight(0.55);

        zoomInBtn.addActionListener(e -> {
            waveformPanel.zoomIn();
            zoomLabel.setText("Zoom: " + waveformPanel.zoomPercent() + "%");
        });
        zoomOutBtn.addActionListener(e -> {
            waveformPanel.zoomOut();
            zoomLabel.setText("Zoom: " + waveformPanel.zoomPercent() + "%");
        });
        zoomResetBtn.addActionListener(e -> {
            waveformPanel.resetZoom();
            zoomLabel.setText("Zoom: " + waveformPanel.zoomPercent() + "%");
        });
        zoomFitBtn.addActionListener(e -> {
            waveformPanel.fitToWidth(vcdScroll.getViewport().getExtentSize().width);
            zoomLabel.setText("Zoom: " + waveformPanel.zoomPercent() + "%");
        });

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.setBackground(BG);
        root.add(controls, BorderLayout.NORTH);
        root.add(mainSplit, BorderLayout.CENTER);
        applyDarkTheme(root, controls, topRow, leftActions, rightActions, bottomRow, statusLabel, maxTimeField,
                netlistEditor, stimulusEditor, netlistScroll, stimScroll, vcdScroll, mainSplit, topSplit);

        loadDefaultExamples(netlistEditor, stimulusEditor);
        return root;
    }

    private static JTextArea makeEditor() {
        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setTabSize(2);
        area.setBackground(INPUT_BG);
        area.setForeground(FG);
        area.setCaretColor(FG);
        area.setSelectionColor(new Color(66, 91, 140));
        area.setBorder(BorderFactory.createLineBorder(BORDER));
        return area;
    }

    private static JScrollPane wrap(String title, Component content) {
        JScrollPane pane = new JScrollPane(content);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), title);
        border.setTitleColor(FG);
        pane.setBorder(border);
        pane.getViewport().setBackground(INPUT_BG);
        return pane;
    }

    // Run the simulation and display the waveform
    private static void runSimulation(
            JTextArea netlistEditor,
            JTextArea stimulusEditor,
            VcdWaveformPanel waveformPanel,
            String[] latestVcd,
            JTextField maxTimeField,
            JLabel statusLabel) {
        long maxTime;
        try {
            maxTime = Long.parseLong(maxTimeField.getText().trim());
            if (maxTime < 0) {
                throw new NumberFormatException("negative");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Max time must be a non-negative integer.", "Invalid max time",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String vcd = SimulationRunner.runToString(netlistEditor.getText(), stimulusEditor.getText(), maxTime);
            latestVcd[0] = vcd;
            waveformPanel.loadFromVcd(vcd);
            statusLabel.setText("Run complete");
        } catch (IOException | IllegalArgumentException ex) {
            statusLabel.setText("Run failed");
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Simulation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void openFileInto(JFrame frame, JTextArea target) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            try {
                target.setText(Files.readString(path));
                target.setCaretPosition(0);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Could not open file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Save the VCD output to a file
    private static void saveTextToFile(JFrame frame, String text) {
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No VCD output to save yet.", "Nothing to save",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            try {
                Files.writeString(path, text);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Could not save file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Load the default examples for the simulation 
    private static void loadDefaultExamples(JTextArea netlistEditor, JTextArea stimulusEditor) {
        try {
            Path netlist = Path.of("examples", "xor_nand.v");
            Path stimulus = Path.of("examples", "xor_nand.stim");
            if (Files.exists(netlist)) {
                netlistEditor.setText(Files.readString(netlist));
            }
            if (Files.exists(stimulus)) {
                stimulusEditor.setText(Files.readString(stimulus));
            }
        } catch (IOException ignored) {
            // Leave editors empty if defaults cannot be loaded.
        }
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing look and feel.
        }
    }

    // Apply the dark theme to the UI 
    private static void applyDarkTheme(Component... components) {
        for (Component component : components) {
            if (component instanceof JPanel panel) {
                panel.setBackground(PANEL_BG);
                panel.setForeground(FG);
            } else if (component instanceof JSplitPane splitPane) {
                splitPane.setBackground(BG);
            } else if (component instanceof JScrollPane scrollPane) {
                scrollPane.setBackground(PANEL_BG);
                scrollPane.getViewport().setBackground(INPUT_BG);
            } else if (component instanceof JLabel label) {
                label.setForeground(FG);
            } else if (component instanceof JButton button) {
                button.setBackground(new Color(52, 57, 69));
                button.setForeground(FG);
                button.setBorder(BorderFactory.createLineBorder(BORDER));
                button.setFocusPainted(false);
            } else if (component instanceof JTextField textField) {
                textField.setBackground(INPUT_BG);
                textField.setForeground(FG);
                textField.setCaretColor(FG);
                textField.setBorder(BorderFactory.createLineBorder(BORDER));
            }
        }
    }
}
