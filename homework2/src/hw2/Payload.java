package hw2;

import java.io.Serializable;

class Payload implements Serializable {

	private double co2;

	Payload(double co2) {
		this.co2 = co2;
	}

	double getCo2() {
		return co2;
	}

	@Override
	public String toString() {
		return String.format("%.2f", co2);
	}
}
