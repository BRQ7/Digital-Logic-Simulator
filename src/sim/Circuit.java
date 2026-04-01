package sim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Circuit {
    private final String moduleName;
    private final Map<String, Wire> wires = new LinkedHashMap<>();
    private final List<Gate> gates = new ArrayList<>();
    private final List<Wire> inputs = new ArrayList<>();
    private final List<Wire> outputs = new ArrayList<>();

    public Circuit(String moduleName) {
        this.moduleName = moduleName;
    }

    public String moduleName() {
        return moduleName;
    }

    public Wire wire(String name) {
        return wires.computeIfAbsent(name, Wire::new);
    }

    public void addInput(String wireName) {
        inputs.add(wire(wireName));
    }

    public void addOutput(String wireName) {
        outputs.add(wire(wireName));
    }

    public void addWire(String wireName) {
        wire(wireName);
    }

    public void addGate(Gate gate) {
        gates.add(gate);
        for (Wire in : gate.inputs()) {
            in.addFanout(gate);
        }
    }

    public Collection<Wire> allWires() {
        return wires.values();
    }

    public List<Gate> gates() {
        return gates;
    }

    public List<Wire> inputs() {
        return inputs;
    }

    public List<Wire> outputs() {
        return outputs;
    }
}
