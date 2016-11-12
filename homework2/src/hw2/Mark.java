package hw2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fhrenic on 11/11/2016.
 */
class Mark implements Serializable, Comparable<Mark> {

	private long logic;
	private final Map<String, Long> vector;

	Mark(){
		this(0);
	}

	Mark(long logic){
		this.logic = logic;
		vector = new HashMap<>();
	}

	void recieve(String who, Mark m) {
		logic = Math.max(logic, m.logic) + 1;
		m.vector.keySet().parallelStream().forEach(
				_k -> vector.compute(_k, (k, v) -> Math.max(m.vector.get(_k), v == null ? 0 : v))
		);
		vector.compute(who, (k, v) -> v + 1); // must exist
	}

	void computeOrSend(String who) {
		++logic;
		vector.compute(who, (k, v) -> (v == null) ? 1 : v + 1);
	}


	@Override
	public int compareTo(Mark o) {
		// compare them by logic time
		return Long.compare(logic, o.logic);
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

	public long getLogic() {
		return logic;
	}

	public Map<String, Long> getVector() {
		return vector;
	}
}
