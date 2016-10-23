package hw1;

import java.io.Serializable;

/**
 * Created by fhrenic on 20/10/2016.
 */
public class Measurement implements Serializable {

	private String name;

	private float value;

	public Measurement(String name, float value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Measurement)) {
			return false;
		}

		Measurement that = (Measurement) o;

		if (Float.compare(that.value, value) != 0) {
			return false;
		}
		return name != null ? name.equals(that.name) : that.name == null;

	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (value != +0.0f ? Float.floatToIntBits(value) : 0);
		return result;
	}

	@Override
	public String toString() {
		return String.format("%s=%.2f", name, value);
	}

	public String getName() {
		return name;
	}

	public float getValue() {
		return value;
	}
}
