package sim;

import java.io.IOException;
import java.nio.file.Path;

// Digital Logic Simulator 
// Author: Brandon Quandt  
public final class Main {


    public static void main(String[] args) {
        // If no arguments are provided, launch the built-in IDE
        if (args.length == 0 || (args.length == 1 && "--gui".equals(args[0]))) {
            LogicSimIde.launch();
            return;
        }

        
        if (args.length != 4) {
            printUsage();
            System.exit(1);
        }

        Path netlistPath = Path.of(args[0]);
        Path stimulusPath = Path.of(args[1]);
        Path vcdPath = Path.of(args[2]);
        long maxTime;

        try {
            maxTime = Long.parseLong(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("max_time must be an integer.");
            printUsage();
            return;
        }

        try {
            SimulationRunner.runToFile(netlistPath, stimulusPath, vcdPath, maxTime);
            System.out.println("Simulation complete. VCD written to: " + vcdPath.toAbsolutePath());
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    // Print the usage of the program 
    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -cp out sim.Main <netlist.v> <stimulus.stim> <out.vcd> <max_time>");
        System.out.println("  java -cp out sim.Main --gui");
        System.out.println();
        System.out.println("Stimulus format: <time> <wire> <0|1|x>");
    }
}
