package sim;

public record Event(long time, Wire wire, LogicValue value) implements Comparable<Event> {
    @Override
    public int compareTo(Event other) {
        return Long.compare(this.time, other.time);
    }
}
