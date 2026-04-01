package sim;

import java.util.List;

// Gate class to represent a gate in the circuit, with a name, type, inputs, output, and delay
// Author: Brandon Quandt 
public final class Gate {
    private final String name;
    private final GateType type;
    private final List<Wire> inputs;
    private final Wire output;
    private final long delay;

    public Gate(String name, GateType type, List<Wire> inputs, Wire output, long delay) {
        this.name = name;
        this.type = type;
        this.inputs = inputs;
        this.output = output;
        this.delay = delay;
    }

    public String name() {
        return name;
    }

    public List<Wire> inputs() {
        return inputs;
    }

    public Wire output() {
        return output;
    }

    public long delay() {
        return delay;
    }

    public LogicValue evaluate() {
        return type.evaluate(inputs);
    }
}
