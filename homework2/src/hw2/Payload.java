package hw2;

import java.io.Serializable;

/**
 * Created by fhrenic on 11/11/2016.
 */
public class Payload implements Serializable {

	private double co2;

	Payload(double co2) {
		this.co2 = co2;
	}

	public double getCo2() {
		return co2;
	}
}
