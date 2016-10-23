/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw1;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author fhrenic
 */
public class Util {

    private static boolean DEBUG = true;

    private static final double LATITUDE_LOW = 15.67;
    private static final double LATITUDE_HIGH = 16.0;
    private static final double LONGITUDE_LOW = 45.75;
    private static final double LONGITUDE_HIGH = 45.85;

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    private static final Random RANDOM = new Random();

    private static final double EPS = 1e-6;

    private Util() {
    }

    /**
     * Print message to sys.err if DEBUG is on
     *
     * @param message
     */
    public static void debug(String message) {
        if (DEBUG) {
            System.err.println(message);
        }
    }

    /**
     * Helper debug method. It creates a string using string format method and
     * calls debug(string)
     *
     * @param fmt
     * @param objects
     */
    public static void debug(String fmt, Object... objects) {
        debug(String.format(fmt, objects));
    }

    /**
     * Deal with exception and exit current thread
     *
     * @param e exception
     */
    public static void except(Exception e) {
        debug(e.getMessage());
    }

    /**
     * Tries to convert given string to float; Returns 0.0f on exception
     *
     * @param s string used to extract number
     * @return float number
     */
    public static float toFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    /**
     * Return current time in seconds
     *
     * @return time in seconds
     */
    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Return double in given range
     *
     * @param lo lower value
     * @param hi higher values
     * @return double in given range
     */
    public static double randomDoubleInRange(double lo, double hi) {
        return RANDOM.nextDouble() * (hi - lo) + lo;
    }

    /**
     * Return random latitude in specified range
     *
     * @return latitude
     */
    public static double randomLatitude() {
        return randomDoubleInRange(LATITUDE_LOW, LATITUDE_HIGH);
    }

    /**
     * Return random longitude in specified range
     *
     * @return longitude
     */
    public static double randomLongitude() {
        return randomDoubleInRange(LONGITUDE_LOW, LONGITUDE_HIGH);
    }

    /**
     * Return local ip address used
     *
     * @return ip address
     */
    public static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * Split string on comma
     *
     * @param s string
     * @return floats
     */
    public static List<Float> stringToFloats(String s) {
        List<Float> measures = new ArrayList<>(6);
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == ',') {
                measures.add(toFloat(sb.toString()));
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        return measures;
    }

}
