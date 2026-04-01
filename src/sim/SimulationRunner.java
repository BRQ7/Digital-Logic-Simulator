package sim;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SimulationRunner {
    private SimulationRunner() {
    }

    // Run the simulation and save the VCD output to a file
    public static void runToFile(Path netlistPath, Path stimulusPath, Path vcdPath, long maxTime) throws IOException {
        String netlistSource = Files.readString(netlistPath);
        String stimulusSource = Files.readString(stimulusPath);
        String vcd = runToString(netlistSource, stimulusSource, maxTime);
        Files.writeString(vcdPath, vcd);
    }

    // Run the simulation and return the VCD output as a string
    public static String runToString(String netlistSource, String stimulusSource, long maxTime) throws IOException {
        Circuit circuit = NetlistParser.parseSource(netlistSource);
        List<StimulusEvent> stimuli = StimulusParser.parseSource(stimulusSource);

        StringWriter stringWriter = new StringWriter();
        try (BufferedWriter writer = new BufferedWriter(stringWriter)) {
            VcdWriter vcd = new VcdWriter(writer);
            vcd.writeHeader(circuit);
            Simulator simulator = new Simulator(circuit);
            simulator.run(stimuli, maxTime, vcd);
            vcd.close();
        }
        return stringWriter.toString();
    }
}
