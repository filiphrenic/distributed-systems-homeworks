package hw2;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Represents one node in a distributed environment.
 */
public class Node {

	/**
	 * Start a new node
	 *
	 * @param args expecting only one
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting only one argument - node name");
		}
		String name = args[0];

		try {
			new Node(name).start();
		} catch (SocketException e) {
			Util.debug("Socket error for %s", name);
		} catch (IllegalArgumentException e) {
			Util.debug("Unknown name for node: %s", name);
		}

	}

	private final String name;
	private final long startTime;

	private final DatagramSocket sender;
	private final DatagramSocket receiver;

	private long lastPacketNum;
	private final Map<Long, Payload> payloads;
	private final Map<String, SocketAddress> nodes;
	private final Map<String, Long> lastAck;

	private final Mark mark;
	private final List<Packet> memory;
	private final List<Double> co2;

	private final EmulatedSystemClock esc;

	private Node(String name) throws SocketException, IllegalArgumentException {

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
			Util.except(ex);
			throw ex;
		}

		mark = new Mark(startTime, name);
		lastPacketNum = -1;
		payloads = Collections.synchronizedMap(new HashMap<>());
		nodes = Config.getNodesFor(name);
		memory = Collections.synchronizedList(new LinkedList<>());
		lastAck = Collections.synchronizedMap(new HashMap<>());
		co2 = Util.readCO2();

		Util.debug("Created new node [%s, %d, %d]", name, addr.getPort(), startTime);
	}

	private void start() {

		// start receiving packets
		new Thread(new Receiver()).start();

		// send packets every minute
		while (true) {
			for (int i = 0; i < Config.LOOP_ITERATIONS; i++) {
				try {
					TimeUnit.SECONDS.sleep(Config.EVERY_N_SECONDS);
					iteration();
				} catch (InterruptedException e) {
					Util.except(e);
				}
			}
			output();
		}
	}

	private void iteration() {
		// create new payload with current measurement
		Payload payload = new Payload(getMeasurement());
		payloads.put(++lastPacketNum, payload);

		// onSend to all nodes
		for (Map.Entry<String, SocketAddress> e : nodes.entrySet()) {

			String nodename = e.getKey();
			SocketAddress sa = e.getValue();
			mark.onSend(name);
			long num = lastAck.getOrDefault(nodename, -1L) + 1L;

			Packet packet = Packet.create(num, mark, name, payloads.get(num));

			try {
				sender.send(packet.toDatagram(sa));
			} catch (IOException ex) {
				Util.except(ex);
			}
		}
	}

	private void output() {
		List<Packet> packets;
		synchronized (this) {
			packets = new LinkedList<>(memory);
			memory.clear();
		}

		String scalarSort = sortPackets(
				packets, (p1, p2) -> p1.getMark().compareScalar(p2.getMark())
		);

		String vectorSort = sortPackets(
				packets, (p1, p2) -> p1.getMark().compareVector(p2.getMark())
		);

		double average = packets.parallelStream()
				.mapToDouble(p -> p.getPayload().getCo2())
				.average().orElse(0.0);

		System.out.println(
				new StringJoiner("\n\t")
						.add("> " + name)
						.add(scalarSort)
						.add(vectorSort)
						.add(String.format("average=%.2f", average))
		);
	}

	private String sortPackets(List<Packet> memory, Comparator<Packet> comp) {
		Collections.sort(memory, comp);
		return memory.stream()
				.map(p -> p.getPayload().toString())
				.collect(Collectors.joining(",", "[", "]"));
	}

	private double getMeasurement() {
		int idx = Math.abs((int) (esc.currentTimeMillis() - startTime)) % co2.size();
		return co2.get(idx);
	}

	private class Receiver implements Runnable {

		private static final int BUFFER_SIZE = 8192;

		@Override
		public void run() {

			byte[] buff = new byte[BUFFER_SIZE];
			DatagramPacket dp = new DatagramPacket(buff, buff.length);
			Packet packet;

			while (true) {

				try {
					Node.this.receiver.receive(dp);
				} catch (SocketTimeoutException e) {
					Util.except(e);
					System.err.println("Receive timeout on " + Node.this.name);
					continue;
				} catch (IOException e) {
					Util.except(e);
					continue;
				}

				Util.debug("Received packet on %s", Node.this.name);
				try {
					packet = Packet.fromDatagram(dp);
				} catch (IOException | ClassNotFoundException e) {
					Util.except(e);
					continue;
				}

				Mark other = packet.getMark();
				if (other == null) {
					continue;
				}
				mark.onReceive(Node.this.name, other);

				String from = packet.getFrom();
				if (from == null || !nodes.containsKey(from)) {
					Util.debug("Didnt find entry for %s", from);
					continue;
				}
				SocketAddress sa = nodes.get(from);
				long packetNum = packet.getNum();

				if (packet.getPayload() == null) {
					// ack
					// update last ack
					Node.this.lastAck.compute(from, (s, l) -> Math.max(packetNum, l == null ? 0 : l));
				} else {
					// data
					// save packet and send ack
					memory.add(packet);
					mark.onSend(Node.this.name);
					try {
						sender.send(Packet.ack(packetNum, mark, name).toDatagram(sa));
					} catch (IOException e) {
						Util.except(e);
					}
				}

			}
		}
	}

}
