package hw2;

import java.io.Serializable;

/**
 * Created by fhrenic on 11/11/2016.
 */
public class Payload implements Serializable {
	public static void main(String[] args) {
		System.out.println(new Payload(25.123213));
	}

	private double co2;

	Payload(double co2) {
		this.co2 = co2;
	}

	public double getCo2() {
		return co2;
	}

	public void setCo2(double co2) {
		this.co2 = co2;
	}

	@Override
	public String toString() {
		return String.format("%.2f", co2);
	}
}
