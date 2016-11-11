package hw2;

import java.util.Random;

/**
 * Created by fhrenic on 11/11/2016.
 */
class Util {

	public static final Random RANDOM = new Random();

	private static EmulatedSystemClock ESC = new EmulatedSystemClock();


	private Util() {
	}

	/**
	 * Return current time in milliseconds
	 *
	 * @return milliseconds
	 */
	public static long currentTimeMillis() {
		return ESC.currentTimeMillis();
	}

	/**
	 * @author Aleksandar
	 */
	private static class EmulatedSystemClock {

		private long startTime;
		private double jitter; //jitter per second,  percentage of deviation per 1 second

		EmulatedSystemClock() {
			startTime = System.currentTimeMillis();
			Random r = new Random();
			jitter = (r.nextInt(400) - 200) / 1000d;
		}

		long currentTimeMillis() {
			long current = System.currentTimeMillis();
			long diff = current - startTime;
			double coef = diff / 1000;
			return startTime + Math.round(diff * Math.pow((1 + jitter), coef));
		}
	}
}
