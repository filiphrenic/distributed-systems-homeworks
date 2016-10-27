/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 *
 * @author fhrenic
 */
public class Run {

    private static final Map<String, Sensor> SENSORS;
    private static final Map<String, Control> CONTROLS;
    private static final NavigableMap<String, String> HELP;

    static {
        SENSORS = new HashMap<>();
        CONTROLS = new HashMap<>();
        HELP = new TreeMap<>();

        CONTROLS.put("create", args -> {
            Sensor s = new Sensor();
            SENSORS.put(s.getUsername(), s);
            System.err.println(s.getUsername());
            return true;
        });
        HELP.put("create", "creates a new sensor and prints out it's name");

        CONTROLS.put("help", args -> {
            HELP.forEach((name, help) -> System.err.printf("%-23s => %s%n", name, help));
            return true;
        });
        HELP.put("help", "shows this descriptions");

        CONTROLS.put("exit", args -> {
            SENSORS.forEach((name, s) -> {
                s.stopRunning();
                s.stopListening();
            });
            return false;
        });
        HELP.put("exit", "stops all sensors and terminates the program");

        CONTROLS.put("listen", (SensorControl) (Sensor s) -> {
            s.listen();
            return true;
        });
        HELP.put("listen <sensor>", "<sensor> starts listening for connections");

        CONTROLS.put("stop-listening", (SensorControl) (Sensor s) -> {
            s.stopListening();
            return true;
        });
        HELP.put("stop-listening <sensor>", "<sensor> stops listening for connections");

        CONTROLS.put("run", (SensorControl) (Sensor s) -> {
            s.run();
            return true;
        });
        HELP.put("run <sensor>", "<sensor> starts sending measurements to server");

        CONTROLS.put("stop-running", (SensorControl) (Sensor s) -> {
            s.stopRunning();
            return true;
        });
        HELP.put("stop-running <sensor>", "<sensor> stops sending measurements to server");

    }

    public static void main(String[] args) {
        Run r = new Run();

        CONTROLS.get("help").control(null);
        System.err.println();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {

            while (true) {
                System.err.print("$ ");
                String cmd = reader.readLine();
                if (cmd == null) {
                    break;
                }
                if (!r.execute(cmd)) {
                    break;
                }
            }

        } catch (IOException e) {
        }
    }

    private boolean execute(String cmd) {

        String[] args = cmd.split("\\s+");
        if (args.length < 1) {
            error("Need at least one argument");
            return true;
        }

        Control c = CONTROLS.get(args[0]);
        if (c == null) {
            error("Unknown command");
            return true;
        }

        try {
            return c.control(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            error(e.getMessage());
        }
        return true;
    }

    private void error(String e) {
        System.err.println(e);
    }

    private interface Control {

        boolean control(String[] arguments);

    }

    private interface SensorControl extends Control {

        boolean controlSensor(Sensor s);

        @Override
        default boolean control(String[] arguments) {
            if (arguments.length != 1) {
                throw new IllegalArgumentException("Need only sensor name");
            }
            Sensor s = SENSORS.get(arguments[0]);
            if (s == null) {
                throw new IllegalArgumentException("No sensor with that name");
            }

            return controlSensor(s);
        }

    }

}
