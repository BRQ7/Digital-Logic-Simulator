package sim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// NetlistParser class to parse the netlist file
// Author: Brandon Quandt 
public final class NetlistParser {
    // Regular expressions for parsing the netlist
    private static final Pattern MODULE = Pattern.compile("^module\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\((.*)\\)$");
    private static final Pattern DECL = Pattern.compile("^(input|output|wire)\\s+(.+)$");
    private static final Pattern GATE = Pattern.compile(
            "^(and|or|nand|nor|xor|buf)(?:#(\\d+))?\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\((.+)\\)$");

    private NetlistParser() {
    }

    public static Circuit parse(Path path) throws IOException {
        String src = Files.readString(path);
        return parseSource(src);
    }


    public static Circuit parseSource(String source) {
        // Remove comments from the source code so we can parse the netlist correctly 
        String noComments = source.replaceAll("//.*", "");
        String[] statements = noComments.split(";");
        Circuit circuit = null; 

        for (String raw : statements) {
            String stmt = raw.trim();
            if (stmt.isEmpty()) {
                continue;
            }

            if (stmt.startsWith("module ")) {
                Matcher m = MODULE.matcher(stmt);
                if (!m.find()) {
                    throw new IllegalArgumentException("Invalid module declaration: " + stmt);
                }
                circuit = new Circuit(m.group(1));
                continue;
            }

            if (stmt.equals("endmodule")) {
                break;
            }

            if (circuit == null) {
                throw new IllegalArgumentException("Netlist must begin with module declaration.");
            }

            Matcher decl = DECL.matcher(stmt);
            if (decl.find()) {
                String kind = decl.group(1);
                for (String name : splitNames(decl.group(2))) {
                    if (kind.equals("input")) {
                        circuit.addInput(name);
                    } else if (kind.equals("output")) {
                        circuit.addOutput(name);
                    } else {
                        circuit.addWire(name);
                    }
                }
                continue;
            }

            Matcher gateMatch = GATE.matcher(stmt);
            if (gateMatch.find()) {
                GateType type = GateType.valueOf(gateMatch.group(1).toUpperCase());
                long delay = gateMatch.group(2) == null ? 0L : Long.parseLong(gateMatch.group(2));
                String gateName = gateMatch.group(3);
                List<String> pins = splitNames(gateMatch.group(4));
                // Check if the gate has at least one input and one output
                if (pins.size() < 2) {
                    throw new IllegalArgumentException("Gate must have output + at least one input: " + stmt);
                }
                // Automatically assign the first pin to the output and the rest to the inputs
                Wire output = circuit.wire(pins.get(0));
                List<Wire> inputs = new ArrayList<>();
                for (int i = 1; i < pins.size(); i++) {
                    inputs.add(circuit.wire(pins.get(i)));
                }
                circuit.addGate(new Gate(gateName, type, inputs, output, delay));
                continue;
            }

            throw new IllegalArgumentException("Unsupported statement: " + stmt);
        }

        if (circuit == null) {
            throw new IllegalArgumentException("No module parsed from netlist.");
        }
        return circuit;
    }

    // Helper method to split a CSV string into a list of strings
    private static List<String> splitNames(String csv) {
        String[] raw = csv.split(",");
        List<String> names = new ArrayList<>();
        for (String token : raw) {
            String n = token.trim();
            if (!n.isEmpty()) {
                names.add(n);
            }
        }
        return names;
    }
}
