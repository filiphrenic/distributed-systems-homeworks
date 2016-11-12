package hw2;


import java.util.concurrent.TimeUnit;

/**
 * @author Aleksandar
 */
class EmulatedSystemClock {

	private long startTime;
	private double jitter;

	EmulatedSystemClock() {
		startTime = System.currentTimeMillis();
		jitter = (Util.RANDOM.nextInt(400) - 200) / 1000d;
	}

	long currentTimeMillis() {
		long diff = System.currentTimeMillis() - startTime; // positive
		return startTime + Math.round(diff * Math.pow((1 + jitter), diff / 1000.0));
	}
}
