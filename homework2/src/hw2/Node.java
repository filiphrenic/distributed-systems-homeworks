package hw2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting only one argument - node name");
		}
		String name = args[0];

		Node node = new Node(name);
	}

	private final String name;
	private final DatagramSocket sender;
	private final DatagramSocket reciever;
	private Mark mark;

	private final List<Packet> memory;
	private final List<Double> co2;

	private Node(String name) throws SocketException {

		this.name = name;
		InetSocketAddress addr = Config.getAddressFor(name);

		if (addr == null) {
			throw new IllegalArgumentException("Node name <" + name + "> not found in configuration");
		}

		long time = new EmulatedSystemClock().currentTimeMillis();

		try {
			double lossRate = Config.NETWORK_LOSS_RATE;
			int averageDelay = Config.NETWORK_AVERAGE_DELAY;
			sender = SimulatedDatagramSocket.client(lossRate, averageDelay);
			reciever = SimulatedDatagramSocket.server(addr.getPort(), lossRate, averageDelay);
		} catch (SocketException ex) {
			Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
			throw ex;
		}

		mark = new Mark(time);
		memory = new LinkedList<>();
		co2 = Util.readCO2();

		Util.debug("Created new node [%s, %d, %d]", name, addr.getPort(), time);
	}

	private void onRecieve(Packet packet) {
		mark.recieve(name, packet.getMark());
	}

	private void onComputeOrSend() {
		mark.computeOrSend(name);
	}

	private double getRandomMeasurement() {
		return Util.chooseRandom(co2).orElse(0.0);
	}

}
