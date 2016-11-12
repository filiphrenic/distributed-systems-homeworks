package hw2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by fhrenic on 11/11/2016.
 */
public class Node {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting only one argument - node name");
		}
		String name = args[0];

		Util.debug("Starting %s...", name);
		Node node = new Node(name);
	}

	private String name;
	private DatagramSocket sender;
	private DatagramSocket reciever;

	private Mark mark; // TODO

	private List<Double> co2;

	Node(String name) {

		this.name = name;
		InetSocketAddress addr = Config.getAddressFor(name);

		if (addr == null) {
			throw new IllegalArgumentException("Node name <" + name + "> not found in configuration");
		}

		long time = new EmulatedSystemClock().currentTimeMillis();
		mark = new Mark(time);

		Util.debug("Created new node <%s> with current time = %d", name, time);

	}

	public void onRecieve(Packet packet) {
		mark.recieve(name, packet.getMark());
	}

	public void onComputeOrSend() {
		mark.computeOrSend(name);
	}

	private double getRandomMeasurement() {
		return Util.chooseRandom(co2).orElse(0.0);
	}

}
