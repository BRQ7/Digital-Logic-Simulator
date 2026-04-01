package sim;

import java.util.ArrayList;
import java.util.List;

// Wire class to represent a wire in the circuit
// Author: Brandon Quandt 
public final class Wire {
    private final String name;
    private LogicValue value = LogicValue.X;
    private final List<Gate> fanout = new ArrayList<>();

    public Wire(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public LogicValue value() {
        return value;
    }

    public void setValue(LogicValue value) {
        this.value = value;
    }

    // Get the list of gates that are connected to this wire
    public List<Gate> fanout() {
        return fanout;
    }
    
    // Add a gate to the list of gates that are connected to this wire
    public void addFanout(Gate gate) {
        fanout.add(gate);
    }
}
