package sim;

public record StimulusEvent(long time, String wireName, LogicValue value) {
}
