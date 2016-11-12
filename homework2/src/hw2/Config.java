package hw2;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by fhrenic on 11/11/2016.
 */
class Config {

	private static final int NUM_OF_NODES = 4;
	private static final Map<String, InetSocketAddress> NODES;

	static final double NETWORK_LOSS_RATE = 0.2;
	static final int NETWORK_AVERAGE_DELAY = 1000;

	static {
		NODES = new HashMap<>();
		for (int i = 0; i < NUM_OF_NODES; i++) {
			NODES.put("node" + i, new InetSocketAddress("127.0.0.1", 10000 + i));
		}
	}

	public static int getNumberOfNodes() {
		return NUM_OF_NODES;
	}

	static Iterable<String> getNodeNames() {
		return NODES.keySet();
	}

	static InetSocketAddress getAddressFor(String nodeName) {
		return NODES.get(nodeName);
	}

	public static Iterable<InetSocketAddress> getNodesFor(String nodeName) {
		return NODES.entrySet().parallelStream()
				.filter(e -> !e.getKey().equals(nodeName))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}

}
