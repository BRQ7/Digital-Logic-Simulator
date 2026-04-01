package sim;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class Simulator {
    private final Circuit circuit;
    private final PriorityQueue<Event> queue = new PriorityQueue<>(Comparator.naturalOrder());

    public Simulator(Circuit circuit) {
        this.circuit = circuit;
    }

    public void run(List<StimulusEvent> stimuli, long maxTime, VcdWriter vcdWriter) throws IOException {
        for (StimulusEvent se : stimuli) {
            Wire wire = circuit.wire(se.wireName());
            queue.add(new Event(se.time(), wire, se.value()));
        }

        // Initialize combinational logic from unknown state.
        for (Gate gate : circuit.gates()) {
            queue.add(new Event(0, gate.output(), gate.evaluate()));
        }

        while (!queue.isEmpty()) {
            Event ev = queue.poll();
            if (ev.time() > maxTime) {
                break;
            }

            Wire wire = ev.wire();
            if (wire.value() == ev.value()) {
                continue;
            }

            wire.setValue(ev.value());
            vcdWriter.valueChange(ev.time(), wire);

            for (Gate gate : wire.fanout()) {
                LogicValue out = gate.evaluate();
                queue.add(new Event(ev.time() + gate.delay(), gate.output(), out));
            }
        }
    }
}
