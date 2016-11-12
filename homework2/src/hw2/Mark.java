package hw2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fhrenic on 11/11/2016.
 */
class Mark implements Serializable{

	private long scalar;
	private final Map<String, Long> vector;

	Mark(){
		this(0);
	}

	Mark(long scalar){
		this.scalar = scalar;
		vector = new HashMap<>();
	}

	void recieve(String who, Mark m) {
		scalar = Math.max(scalar, m.scalar) + 1;
		m.vector.keySet().parallelStream().forEach(
				_k -> vector.compute(_k, (k, v) -> Math.max(m.vector.get(_k), v == null ? 0 : v))
		);
		vector.compute(who, (k, v) -> v + 1); // must exist
	}

	void computeOrSend(String who) {
		++scalar;
		vector.compute(who, (k, v) -> (v == null) ? 1 : v + 1);
	}

	public int compareScalar(Mark o) {
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
		int less = 0;
		int same = 0;
		int more = 0;

		for (String node : vector.keySet()) {
			if (!other.vector.containsKey(node)) {
				continue;
			}
			long v1 = this.vector.get(node);
			long v2 = other.vector.get(node);

			if (v1 < v2) {
				less++;
			} else if (v1 > v2) {
				more++;
			} else {
				same++;
			}

			if (less * more != 0) {
				return 0;
			}
		}

		if (same == 0) {
			return less > 0 ? -2 : 2;
		} else {
			return less > 0 ? -1 : 1;
		}
	}

	public long getScalar() {
		return scalar;
	}

	public Map<String, Long> getVector() {
		return vector;
	}
}
