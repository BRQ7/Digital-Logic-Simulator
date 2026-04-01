package sim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// StimulusParser class to parse the stimulus file, used to set the initial values of the wires
// Author: Brandon Quandt 
public final class StimulusParser {
    private StimulusParser() {
    }

    public static List<StimulusEvent> parse(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        return parseLines(lines);
    }

    public static List<StimulusEvent> parseSource(String source) {
        String[] rawLines = source.split("\\R", -1);
        List<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            lines.add(line);
        }
        return parseLines(lines);
    }

    private static List<StimulusEvent> parseLines(List<String> lines) {
        List<StimulusEvent> events = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripComment(lines.get(i)).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid stimulus line " + (i + 1) + ": " + lines.get(i));
            }
            long time = Long.parseLong(parts[0]);
            String wire = parts[1];
            String bit = parts[2];
            if (!"0".equals(bit) && !"1".equals(bit) && !"x".equalsIgnoreCase(bit)) {
                throw new IllegalArgumentException("Stimulus bit must be 0/1/x at line " + (i + 1));
            }
            events.add(new StimulusEvent(time, wire, LogicValue.fromBit(Character.toLowerCase(bit.charAt(0)))));
        }
        events.sort(Comparator.comparingLong(StimulusEvent::time));
        return events;
    }

    private static String stripComment(String line) {
        int idx = line.indexOf("//");
        if (idx >= 0) {
            return line.substring(0, idx);
        }
        return line;
    }
}
