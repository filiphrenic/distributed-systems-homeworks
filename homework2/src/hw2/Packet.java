package hw2;

import java.io.*;
import java.net.DatagramPacket;
import java.net.SocketAddress;


class Packet implements Serializable {

	private long num;
	private Mark mark;
	private Payload payload;

	static Packet create(long num, Mark mark, Payload p) {
		return new Packet(num, mark, p);
	}

	static Packet ack(long num, Mark mark) {
		return new Packet(num, mark, null);
	}

	private Packet(long num, Mark mark, Payload payload) {
		this.num = num;
		this.mark = mark;
		this.payload = payload;
	}

	DatagramPacket toDatagram(SocketAddress sa) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
		     ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(this);
			byte[] arr = bos.toByteArray();

			return new DatagramPacket(arr, arr.length, sa);
		}
	}

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

	long getNum() {
		return num;
	}

	Mark getMark() {
		return mark;
	}

	Payload getPayload() {
		return payload;
	}

}
