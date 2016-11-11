package hw2;


/**
 * @author Aleksandar
 */
public class EmulatedSystemClock {

	private long startTime;
	private double jitter; //jitter per second,  percentage of deviation per 1 second

	EmulatedSystemClock() {
		startTime = System.currentTimeMillis();
		jitter = (Util.RANDOM.nextInt(400) - 200) / 1000d;
	}

	long currentTimeMillis() {
		long diff = System.currentTimeMillis() - startTime;
		return startTime + Math.round(diff * Math.pow((1 + jitter), diff / 1000.0));
	}
}
