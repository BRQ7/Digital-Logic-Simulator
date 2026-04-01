package sim;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// VcdWaveformPanel class to display the waveform of the simulation
// Author: Brandon Quandt 
public final class VcdWaveformPanel extends JPanel {
    private static final int LEFT_MARGIN = 140;
    private static final int TOP_MARGIN = 34;
    private static final int ROW_HEIGHT = 30;
    private static final int LANE_HEIGHT = 16;
    private static final int RIGHT_PADDING = 40;
    private static final int BOTTOM_PADDING = 40;
    private static final int DEFAULT_PX_PER_NS = 14;
    private static final int MIN_PX_PER_NS = 4;
    private static final int MAX_PX_PER_NS = 64;

    private static final Color BG_COLOR = new Color(24, 26, 31);
    private static final Color AXIS_COLOR = new Color(130, 136, 150);
    private static final Color GRID_COLOR = new Color(52, 56, 66);
    private static final Color LABEL_COLOR = new Color(218, 223, 233);
    private static final Color ROW_DIVIDER_COLOR = new Color(44, 48, 57);

    private List<String> signalNames = List.of();
    private Map<String, List<Sample>> waveform = Map.of();
    private long maxTime = 1;
    private int pxPerNs = DEFAULT_PX_PER_NS;

    public VcdWaveformPanel() {
        setBackground(BG_COLOR);
    }

    public void loadFromVcd(String vcdSource) {
        ParsedVcd parsed = parse(vcdSource);
        this.signalNames = parsed.signalNames;
        this.waveform = parsed.waveform;
        this.maxTime = Math.max(1L, parsed.maxTime);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        int width = LEFT_MARGIN + RIGHT_PADDING + (int) Math.max(300L, maxTime * pxPerNs);
        int height = TOP_MARGIN + BOTTOM_PADDING + Math.max(1, signalNames.size()) * ROW_HEIGHT;
        return new Dimension(width, height);
    }

    public void zoomIn() {
        setPxPerNs(pxPerNs + 2);
    }

    public void zoomOut() {
        setPxPerNs(pxPerNs - 2);
    }

    public void resetZoom() {
        setPxPerNs(DEFAULT_PX_PER_NS);
    }

    public void fitToWidth(int viewportWidth) {
        if (viewportWidth <= LEFT_MARGIN + RIGHT_PADDING + 10) {
            return;
        }
        int plotWidth = viewportWidth - LEFT_MARGIN - RIGHT_PADDING;
        int suggested = (int) Math.max(MIN_PX_PER_NS, Math.min(MAX_PX_PER_NS, plotWidth / Math.max(1L, maxTime)));
        setPxPerNs(suggested);
    }

    public int zoomPercent() {
        return (int) Math.round((pxPerNs * 100.0) / DEFAULT_PX_PER_NS);
    }

    private void setPxPerNs(int value) {
        int clamped = Math.max(MIN_PX_PER_NS, Math.min(MAX_PX_PER_NS, value));
        if (clamped == pxPerNs) {
            return;
        }
        pxPerNs = clamped;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            paintAxis(g2);
            paintSignals(g2);
        } finally {
            g2.dispose();
        }
    }

    private void paintAxis(Graphics2D g2) {
        int plotTop = TOP_MARGIN - 10;
        int plotBottom = TOP_MARGIN + Math.max(1, signalNames.size()) * ROW_HEIGHT;
        int plotRight = LEFT_MARGIN + (int) (maxTime * pxPerNs);

        g2.setColor(AXIS_COLOR);
        g2.drawLine(LEFT_MARGIN, plotTop, LEFT_MARGIN, plotBottom);
        g2.drawLine(LEFT_MARGIN, plotTop, plotRight, plotTop);

        g2.setColor(LABEL_COLOR);
        g2.drawString("Time (ns)", LEFT_MARGIN + 6, TOP_MARGIN - 16);

        long tickStep = pickTickStep(maxTime);
        for (long t = 0; t <= maxTime; t += tickStep) {
            int x = LEFT_MARGIN + (int) (t * pxPerNs);
            g2.setColor(GRID_COLOR);
            g2.drawLine(x, plotTop + 1, x, plotBottom);
            g2.setColor(AXIS_COLOR);
            g2.drawLine(x, plotTop - 4, x, plotTop + 2);
            g2.drawString(Long.toString(t), x - 8, plotTop - 8);
        }
    }

    private void paintSignals(Graphics2D g2) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setStroke(new BasicStroke(2f));

        for (int i = 0; i < signalNames.size(); i++) {
            String name = signalNames.get(i);
            List<Sample> samples = waveform.getOrDefault(name, List.of());

            int rowY = TOP_MARGIN + i * ROW_HEIGHT;
            int yHigh = rowY;
            int yLow = rowY + LANE_HEIGHT;
            int yMid = rowY + (LANE_HEIGHT / 2);

            g2.setColor(LABEL_COLOR);
            g2.drawString(name, LEFT_MARGIN - 12 - fm.stringWidth(name), rowY + 12);

            g2.setColor(ROW_DIVIDER_COLOR);
            g2.drawLine(LEFT_MARGIN, rowY + ROW_HEIGHT - 4, LEFT_MARGIN + (int) (maxTime * pxPerNs), rowY + ROW_HEIGHT - 4);

            if (samples.isEmpty()) {
                continue;
            }

            for (int s = 0; s < samples.size(); s++) {
                Sample current = samples.get(s);
                long start = current.time;
                long end = (s + 1 < samples.size()) ? samples.get(s + 1).time : maxTime;
                if (end < start) {
                    continue;
                }

                int x1 = LEFT_MARGIN + (int) (start * pxPerNs);
                int x2 = LEFT_MARGIN + (int) (end * pxPerNs);
                int y = valueY(current.value, yHigh, yLow, yMid);

                g2.setColor(valueColor(current.value));
                if (x2 > x1) {
                    g2.drawLine(x1, y, x2, y);
                }

                if (s + 1 < samples.size()) {
                    Sample next = samples.get(s + 1);
                    int nextY = valueY(next.value, yHigh, yLow, yMid);
                    if (nextY != y) {
                        g2.drawLine(x2, y, x2, nextY);
                    }
                }
            }
        }
    }

    private static int valueY(char value, int yHigh, int yLow, int yMid) {
        if (value == '1') {
            return yHigh;
        }
        if (value == '0') {
            return yLow;
        }
        return yMid;
    }

    private static Color valueColor(char value) {
        if (value == '1') {
            return new Color(14, 122, 54);
        }
        if (value == '0') {
            return new Color(22, 86, 192);
        }
        return new Color(176, 106, 0);
    }

    private static long pickTickStep(long maxTime) {
        if (maxTime <= 20) {
            return 1;
        }
        if (maxTime <= 100) {
            return 5;
        }
        if (maxTime <= 500) {
            return 20;
        }
        if (maxTime <= 2000) {
            return 100;
        }
        return 500;
    }

    private static ParsedVcd parse(String source) {
        Map<String, String> idToName = new LinkedHashMap<>();
        Map<String, Character> currentById = new LinkedHashMap<>();
        Map<String, List<Sample>> byName = new LinkedHashMap<>();
        long currentTime = 0;
        long maxSeenTime = 0;

        String[] lines = source.split("\\R");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("$var")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    String id = parts[3];
                    String name = parts[4];
                    idToName.put(id, name);
                    byName.putIfAbsent(name, new ArrayList<>());
                }
                continue;
            }
            if (line.charAt(0) == '#') {
                currentTime = Long.parseLong(line.substring(1));
                maxSeenTime = Math.max(maxSeenTime, currentTime);
                continue;
            }
            if (line.charAt(0) == '0' || line.charAt(0) == '1' || line.charAt(0) == 'x' || line.charAt(0) == 'X') {
                char value = Character.toLowerCase(line.charAt(0));
                String id = line.substring(1).trim();
                String name = idToName.get(id);
                if (name == null) {
                    continue;
                }
                char prev = currentById.getOrDefault(id, '?');
                if (prev != value) {
                    byName.computeIfAbsent(name, k -> new ArrayList<>()).add(new Sample(currentTime, value));
                    currentById.put(id, value);
                }
            }
        }

        List<String> signalNames = new ArrayList<>(byName.keySet());
        return new ParsedVcd(signalNames, byName, maxSeenTime);
    }

    private record Sample(long time, char value) {
    }

    private record ParsedVcd(List<String> signalNames, Map<String, List<Sample>> waveform, long maxTime) {
    }
}
