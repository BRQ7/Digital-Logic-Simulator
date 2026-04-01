package sim;

import java.util.List;

// GateType class to represent the type of gate in the circuit
// Author: Brandon Quandt 
public enum GateType {
    AND,
    OR,
    NAND,
    NOR,
    XOR,
    NOT,
    BUF;
    // TODO: Add custom primitive gates like XNOR, etc. 

    public LogicValue evaluate(List<Wire> inputs) {
        return switch (this) {
            case AND -> andEval(inputs);
            case OR -> orEval(inputs);
            case NAND -> invert(andEval(inputs));
            case NOR -> invert(orEval(inputs));
            case XOR -> xorEval(inputs);
            case NOT -> notEval(inputs);
            case BUF -> bufEval(inputs);
        };
    }

    private static LogicValue andEval(List<Wire> inputs) {
        boolean seenX = false;
        for (Wire w : inputs) {
            // Short circuit evaluation for AND gate
            if (w.value() == LogicValue.ZERO) {
                return LogicValue.ZERO;
            }
            if (w.value() == LogicValue.X) {
                seenX = true;
            }
        }
        return seenX ? LogicValue.X : LogicValue.ONE;
    }

    private static LogicValue orEval(List<Wire> inputs) {
        boolean seenX = false;
        for (Wire w : inputs) {
            // Short circuit evaluation for OR gate
            if (w.value() == LogicValue.ONE) {
                return LogicValue.ONE;
            }
            if (w.value() == LogicValue.X) {
                seenX = true;
            }
        }
        return seenX ? LogicValue.X : LogicValue.ZERO;
    }

    private static LogicValue xorEval(List<Wire> inputs) {
        int ones = 0;
        for (Wire w : inputs) {
            if (w.value() == LogicValue.X) {
                return LogicValue.X;
            }
            if (w.value() == LogicValue.ONE) {
                ones++;
            }
        }
        return (ones % 2 == 0) ? LogicValue.ZERO : LogicValue.ONE;
    }

    private static LogicValue notEval(List<Wire> inputs) {
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("NOT gate requires exactly one input.");
        }
        return invert(inputs.get(0).value());
    }

    private static LogicValue bufEval(List<Wire> inputs) {
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("BUF gate requires exactly one input.");
        }
        return inputs.get(0).value();
    }

    private static LogicValue invert(LogicValue v) {
        return switch (v) {
            case ZERO -> LogicValue.ONE;
            case ONE -> LogicValue.ZERO;
            case X -> LogicValue.X;
        };
    }
}
