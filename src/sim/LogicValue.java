package sim;

// LogicValue class to represent the value of a wire in the circuit
// Author: Brandon Quandt 
public enum LogicValue {
    ZERO('0'),
    ONE('1'),
    X('x');

    private final char vcdChar;

    LogicValue(char vcdChar) {
        this.vcdChar = vcdChar;
    }

    public char toVcdChar() {
        return vcdChar;
    }

    // Convert a character to a LogicValue
    public static LogicValue fromBit(char c) {
        if (c == '0') {
            return ZERO;
        }
        if (c == '1') {
            return ONE;
        }
        return X;
    }
}
