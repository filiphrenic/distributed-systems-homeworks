package hw2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Marks (clock) in distributed systems.
 */
class Mark implements Serializable {

	private long scalar;
	private final Map<String, Long> vector;

	Mark(long scalar, String who) {
		vector = new HashMap<>();
		this.scalar = scalar;
		vector.put(who, 0L);
	}

	synchronized void onReceive(String who, Mark m) {
		scalar = Math.max(scalar, m.scalar) + 1;
		m.vector.keySet().parallelStream().forEach(
				_k -> vector.compute(_k, (k, v) -> Math.max(m.vector.get(_k), v == null ? 0 : v))
		);
		vector.compute(who, (k, v) -> v + 1); // will exist
	}

	synchronized void onSend(String who) {
		++scalar;
		vector.compute(who, (k, v) -> (v == null) ? 1 : v + 1);
	}

	int compareScalar(Mark o) {
		// compare them by scalar time
		return Long.compare(scalar, o.scalar);
	}

	/**
	 * Return:
	 * -2 if this <  other
	 * -1 if this <= other
	 * 0 if   concurent
	 * 1 if this >= other
	 * 2 if this >  other
	 *
	 * @param other other mark
	 * @return comparison result
	 */
	public int compareVector(Mark other) {

		/*
		 * comp[0] => number of elements in this vector that are less than the element in second vector
		 * comp[1] => number of elements in this vector that are the same as an element in second vector
		 * comp[2] => number of elements in this vector that are bigger than the element in second vector
		 */
		final int comp[] = new int[]{0, 0, 0};

		for (String node : vector.keySet()) {
			if (!other.vector.containsKey(node)) {
				continue;
			}
			long v1 = this.vector.get(node);
			long v2 = other.vector.get(node);

			++comp[(int) Math.signum(Long.compare(v1, v2)) + 1];

			if (comp[0] * comp[2] != 0) {
				return 0;
			}
		}

		if (comp[0] * comp[2] > 0) {
			return 0;
		}

		int ret = comp[1] > 0 ? 1 : 2;

		if (comp[0] > 0) {
			return -ret;
		} else if (comp[0] > 0) {
			return ret;
		} else {
			return 0;
		}

	}

}
