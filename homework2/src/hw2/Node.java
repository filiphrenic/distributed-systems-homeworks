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

	private final EmulatedSystemClock esc;

	private final String name;
	private final long startTime;

	private final DatagramSocket sender;
	private final DatagramSocket receiver;
	private final Map<String, SocketAddress> nodes;

	private final Mark mark;
	private int lastPacketNum; // last packet sent

	private final Map<Integer, Packet> packets;
	private final Map<String, BitSet> acks; // indexes which packet has been acked

	private final Map<Integer, Packet> memory;
	private final List<Double> co2;


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

		lastPacketNum = -1;
		mark = new Mark(startTime, name);

		nodes = Config.getNodesFor(name);
		acks = Collections.synchronizedMap(new HashMap<>());
		packets = Collections.synchronizedMap(new HashMap<>());

		memory = Collections.synchronizedMap(new HashMap<>());
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
		packNew();

		for (Map.Entry<String, SocketAddress> e : nodes.entrySet()) {
			// send to given node

			String nodename = e.getKey();
			SocketAddress sa = e.getValue();

			BitSet bs = acks.getOrDefault(nodename, new BitSet());
			int xy = 0;
			for (int idx = bs.nextClearBit(0); idx <= lastPacketNum; idx = bs.nextClearBit(idx + 1)) {
				try {
					sender.send(packets.get(idx).toDatagram(sa));
				} catch (IOException ex) {
					Util.except(ex);
				}
				xy++;
			}
			Util.debug("%s sent %d packets to %s%n", name, xy, nodename);
		}
	}

	/**
	 * Create new packet that will be sent
	 */
	private void packNew() {
		++lastPacketNum;
		mark.onSend(name);
		Payload payload = new Payload(getMeasurement());
		Packet packet = Packet.create(lastPacketNum, mark, name, payload);
		packets.put(lastPacketNum, packet);
	}


	private void output() {
		List<Packet> packets;
		synchronized (this) {
			packets = new LinkedList<>(memory.values());
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
					receiver.receive(dp);
				} catch (SocketTimeoutException e) {
					Util.except(e);
					System.err.println("Receive timeout on " + name);
					continue;
				} catch (IOException e) {
					Util.except(e);
					continue;
				}

				Util.debug("Received packet on %s", name);
				try {
					packet = Packet.fromDatagram(dp);
				} catch (IOException | ClassNotFoundException e) {
					Util.except(e);
					continue;
				}

				// update marks
				Mark other = packet.getMark();
				mark.onReceive(name, other);

				String from = packet.getFrom();
				if (from == null || !nodes.containsKey(from)) {
					Util.debug("Didnt find entry for %s", from);
					continue;
				}
				SocketAddress sa = nodes.get(from);
				int packetNum = packet.getNum();

				if (packet.getPayload() == null) {
					// ack
					// update ack bitmask
					acks.putIfAbsent(from, new BitSet());
					acks.get(from).set(packetNum);
				} else {
					// data
					// save packet and send ack
					memory.putIfAbsent(packetNum, packet);
					mark.onSend(name);
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
