package sim;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// VcdWriter class to write the VCD file for the simulation, VCD can be used to view the waveform of the simulation in GTKWave or the built-in waveform viewer
// Author: Brandon Quandt 
public final class VcdWriter {
    private final Writer out;
    private final Map<Wire, String> idByWire = new LinkedHashMap<>();
    private long lastTime = -1;

    public VcdWriter(Writer out) {
        this.out = out;
    }

    public void writeHeader(Circuit circuit) throws IOException {
        out.write("$date\n");
        out.write("  " + LocalDateTime.now() + "\n");
        out.write("$end\n");
        out.write("$version\n");
        out.write("  Java Event-Driven Logic Simulator\n");
        out.write("$end\n");
        out.write("$timescale 1ns $end\n");
        out.write("$scope module " + circuit.moduleName() + " $end\n");

        int idx = 0;
        for (Wire wire : circuit.allWires()) {
            String id = toId(idx++);
            idByWire.put(wire, id);
            out.write("$var wire 1 " + id + " " + wire.name() + " $end\n");
        }
        out.write("$upscope $end\n");
        out.write("$enddefinitions $end\n");
        out.write("$dumpvars\n");
        for (Wire wire : circuit.allWires()) {
            out.write(wire.value().toVcdChar() + idByWire.get(wire) + "\n");
        }
        out.write("$end\n");
        out.flush();
    }

    public void valueChange(long time, Wire wire) throws IOException {
        if (time != lastTime) {
            out.write("#" + time + "\n");
            lastTime = time;
        }
        out.write(wire.value().toVcdChar() + idByWire.get(wire) + "\n");
    }

    public void close() throws IOException {
        out.flush();
        out.close();
    }

    private static String toId(int idx) {
        // Use printable range from '!' to '~'
        StringBuilder sb = new StringBuilder();
        int base = 94;
        int value = idx;
        do {
            sb.append((char) ('!' + (value % base)));
            value = value / base;
        } while (value > 0);
        return sb.toString();
    }
}
