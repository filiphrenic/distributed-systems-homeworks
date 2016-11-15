package hw2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that is responsible for configuration parameters.
 */
class Config {

	private static final int NUM_OF_NODES = 4;
	private static final Map<String, InetSocketAddress> NODES;

	static final double NETWORK_LOSS_RATE = 0.2;
	static final int NETWORK_AVERAGE_DELAY = 1000;

	// Repeat process every-N-seconds for loop-iterations times
	static final int EVERY_N_SECONDS = 1;
	static final int LOOP_ITERATIONS = 5;

	static {
		NODES = new HashMap<>();
		for (int i = 0; i < NUM_OF_NODES; i++) {
			NODES.put("node" + i, new InetSocketAddress("127.0.0.1", 10000 + i));
		}
	}

	static Iterable<String> getNodeNames() {
		return NODES.keySet();
	}

	static InetSocketAddress getAddressFor(String nodeName) {
		return NODES.get(nodeName);
	}

	static Map<String, SocketAddress> getNodesFor(String nodeName) {
		return NODES.entrySet().parallelStream()
				.filter(e -> !e.getKey().equals(nodeName))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
