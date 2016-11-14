package hw2;

/**
 * Emulated system clock, adds jitter to current system clock. Used for simulating different times.
 *
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
