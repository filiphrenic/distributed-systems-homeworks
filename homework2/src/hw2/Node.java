package hw2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

	public static void main(String[] args) throws IOException, InterruptedException {
//		if (args.length != 1) {
//			throw new IllegalArgumentException("Expecting only one argument - node name");
//		}
//		String name = args[0];
//
//		Node node = new Node(name);

		new Thread(() -> {
			try {
				new Node("node1").bbb();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
		Thread.sleep(1123);
		new Node("node0").aaa();

	}

	private final String name;
	private final DatagramSocket sender;
	private final DatagramSocket receiver;

	private final long startTime;
	private final EmulatedSystemClock esc;
	private Mark mark;

	private final List<Packet> memory;
	private final List<Double> co2;

	private Node(String name) throws SocketException {

		this.name = name;
		InetSocketAddress addr = Config.getAddressFor(name);

		if (addr == null) {
			throw new IllegalArgumentException("Node name <" + name + "> not found in configuration");
		}

		esc = new EmulatedSystemClock();
		startTime = esc.currentTimeMillis();

		try {
			double lossRate = Config.NETWORK_LOSS_RATE;
			int averageDelay = Config.NETWORK_AVERAGE_DELAY;
			sender = SimulatedDatagramSocket.client(lossRate, averageDelay);
			receiver = SimulatedDatagramSocket.server(addr.getPort(), lossRate, averageDelay);
		} catch (SocketException ex) {
			Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
			throw ex;
		}

		mark = new Mark(startTime);
		memory = new LinkedList<>();
		co2 = Util.readCO2();

		Util.debug("Created new node [%s, %d, %d]", name, addr.getPort(), startTime);
	}

	private void aaa() throws IOException {
		Payload p = new Payload(0);
		for (int i = 0;i<5;i++){
			p.setCo2(i);
			DatagramPacket dp = Packet.create(null, p).toDatagram(new InetSocketAddress("localhost", 10001));
			sender.send(dp);
		}
	}

	private void bbb() throws IOException {
		byte[] buff = new byte[8192];
		DatagramPacket dp = new DatagramPacket(buff, buff.length);
		while (true) {
			receiver.receive(dp);
			Packet p = Packet.fromDatagram(dp);
			System.out.println("RECIEVED " + p);
		}
	}

	private void output() {

		Collection<Packet> packets = new LinkedList<>(memory);
		memory.clear();

		String scalarSort = sortPackets(
				packets, "scalar",
				(p1, p2) -> p1.getMark().compareScalar(p2.getMark())
		);

		String vectorSort = sortPackets(
				packets, "vector",
				(p1, p2) -> p1.getMark().getVector().get(name).compareTo(p2.getMark().getVector().get(name))
		);

		double average = packets.parallelStream()
				.mapToDouble(p -> p.getPayload().getCo2())
				.average().orElse(0.0);

		System.out.println(
				new StringJoiner("\n").add(scalarSort).add(vectorSort).add(name + " average=" + average)
		);
	}

	private String sortPackets(Collection<Packet> memory, String sortType, Comparator<Packet> comp) {
		Set<Packet> packets = new TreeSet<>(comp);
		packets.addAll(memory);
		StringJoiner sj = new StringJoiner(", ", name + " " + sortType, "");
		packets.forEach(p -> sj.add(p.getPayload().toString()));
		return sj.toString();
	}

	private void onRecieve(Packet packet) {
		mark.recieve(name, packet.getMark());
	}

	private void onComputeOrSend() {
		mark.computeOrSend(name);
	}

	private double getRandomMeasurement() {
		int idx = (int) ((esc.currentTimeMillis() - startTime) % co2.size());
		return co2.get(idx);
	}

}
