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

/**
 *
 * @author fhrenic
 */
public class Run {

    private static final Map<String, Sensor> SENSORS;
    private static final Map<String, Control> CONTROLS;

    static {
        SENSORS = new HashMap<>();
        CONTROLS = new HashMap<>();

        CONTROLS.put("create", args -> {
            Sensor s = new Sensor();
            SENSORS.put(s.getUsername(), s);
            return true;
        });
        CONTROLS.put("help", args -> {
            CONTROLS.forEach((name, c) -> System.err.println(name));
            return true;
        });
        CONTROLS.put("exit", args -> false);
        CONTROLS.put("listen", (SensorControl) (Sensor s) -> {
            s.listen();
            return true;
        });
        CONTROLS.put("stop-listening", (SensorControl) (Sensor s) -> {
            s.stopListening();
            return true;
        });
        CONTROLS.put("run", (SensorControl) (Sensor s) -> {
            s.run();
            return true;
        });
        CONTROLS.put("stop-running", (SensorControl) (Sensor s) -> {
            s.stopRunning();
            return true;
        });
    }

    public static void main(String[] args) {
        Run r = new Run();

        CONTROLS.get("help").control(null);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {

            while (true) {
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
