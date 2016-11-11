package hw2;

import java.io.*;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fhrenic on 11/11/2016.
 */
public class Packet implements Serializable {

	private long scalarMark;
	private long[] vectorMark;
	private Payload payload;

	public static Packet create(long scalarMark, long[] vectorMark, double co2) {
		return new Packet(scalarMark, vectorMark, new Payload(co2));
	}

	public static Packet ack(long scalarMark, long[] vectorMark) {
		return new Packet(scalarMark, vectorMark, null);
	}

	private Packet(long scalarMark, long[] vectorMark, Payload payload) {
		this.scalarMark = scalarMark;
		this.vectorMark = Arrays.copyOf(vectorMark, vectorMark.length);
		this.payload = payload;
	}

	public DatagramPacket toDatagram(SocketAddress sa) {

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
		     ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(this);
			byte[] arr = bos.toByteArray();

			return new DatagramPacket(arr, arr.length, sa);

		} catch (IOException e) {
			Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, e);
		}

		return null;
	}


	public static Packet fromDatagram(DatagramPacket dp) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(dp.getData());
		     ObjectInput in = new ObjectInputStream(bis)) {

			return (Packet) in.readObject();

		} catch (ClassNotFoundException | IOException e) {
			Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, e);
		}

		return null;
	}

	public long getScalarMark() {
		return scalarMark;
	}

	public long[] getVectorMark() {
		return vectorMark;
	}

	public Payload getPayload() {
		return payload;
	}

}
