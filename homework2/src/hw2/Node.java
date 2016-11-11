package hw2;

import java.util.List;

/**
 * Created by fhrenic on 11/11/2016.
 */
public class Node {

	private String name;
	private List<Double> co2;
	private Mark mark; // TODO


	public void onRecieve(Mark m) {
		mark.recieve(name, m);
	}

	public void onComputeOrSend() {
		mark.computeOrSend(name);
	}

	private double getRandomMeasurement() {
		return Util.chooseRandom(co2).orElse(0.0);
	}

}
