package hw2;

import java.io.*;
import java.net.DatagramPacket;
import java.net.SocketAddress;


/**
 * Packet that is sent between nodes.
 */
class Packet implements Serializable {

	private int num;
	private Mark mark;
	private String from;
	private Payload payload;

	static Packet create(int num, Mark mark, String from, Payload p) {
		return new Packet(num, mark, from, p);
	}

	static Packet ack(int num, Mark mark, String from) {
		return new Packet(num, mark, from, null);
	}

	private Packet(int num, Mark mark, String from, Payload payload) {
		this.num = num;
		this.mark = mark;
		this.from = from;
		this.payload = payload;
	}

	/**
	 * Create a datagram packet using this packet. It will be sent to given address
	 *
	 * @param sa socket address to send to
	 * @return datagram packet
	 * @throws IOException Any of the usual Input/Output related exceptions.
	 */
	DatagramPacket toDatagram(SocketAddress sa) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
		     ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(this);
			byte[] arr = bos.toByteArray();

			return new DatagramPacket(arr, arr.length, sa);
		}
	}

	/**
	 * Reads Packet object from recieved datagram packet.
	 *
	 * @param dp datagram packet
	 * @return read packet object
	 * @throws IOException            Any of the usual Input/Output related exceptions.
	 * @throws ClassNotFoundException Error on reading object from bytes
	 */
	static Packet fromDatagram(DatagramPacket dp) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(dp.getData());
		     ObjectInput in = new ObjectInputStream(bis)) {
			return (Packet) in.readObject();
		}
	}

	@Override
	public String toString() {
		return payload.toString();
	}

	int getNum() {
		return num;
	}

	Mark getMark() {
		return mark;
	}

	public String getFrom() {
		return from;
	}

	Payload getPayload() {
		return payload;
	}

}
