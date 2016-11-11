package hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by fhrenic on 11/11/2016.
 */
class Util {

	static final Random RANDOM = new Random();
	private static final String MEASUREMENTS_FILENAME = "mjerenja.csv";
	private static final boolean DEBUG = true;

	public static <T> Optional<T> chooseRandom(List<T> list) {
		int n = list.size();
		if (n == 0) {
			return Optional.empty();
		}
		return Optional.of(list.get(RANDOM.nextInt(n)));
	}

	public static List<Double> readCO2() {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						Util.class.getResourceAsStream("/" + MEASUREMENTS_FILENAME), StandardCharsets.UTF_8
				)
		)) {
			reader.readLine(); // read header
			return reader.lines().map(Util::getCO2).collect(Collectors.toCollection(ArrayList::new));
		} catch (IOException e) {
			Util.except(e);
			System.exit(1);
			return null;
		}
	}


	/**
	 * Print message to sys.err if DEBUG is on
	 *
	 * @param message message
	 */
	static void debug(String message) {
		if (DEBUG) {
			System.err.println(message);
		}
	}

	/**
	 * Helper debug method. It creates a string using string format method and
	 * calls debug(string)
	 *
	 * @param fmt     format
	 * @param objects objects
	 */
	static void debug(String fmt, Object... objects) {
		debug(String.format(fmt, objects));
	}

	/**
	 * Deal with exception and exit current thread
	 *
	 * @param e exception
	 */
	static void except(Exception e) {
		debug(e.getMessage());
	}

	/**
	 * Tries to convert given string to double; Returns 0.0 on exception
	 *
	 * @param s string used to extract number
	 * @return double number
	 */
	private static double toDouble(String s) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}


	/**
	 * Split string on comma
	 *
	 * @param row string
	 * @return co2 value
	 */
	private static double getCO2(String row) {
		int count = -1;
		StringBuilder sb = new StringBuilder();
		for (char c : row.toCharArray()) {
			if (c == ',') {
				if (++count == 3) {
					return toDouble(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(c);
			}
		}
		return -1;
	}

	private Util() {
	}

}
