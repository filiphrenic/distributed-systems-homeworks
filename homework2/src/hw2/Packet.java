package hw2;

import java.io.*;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fhrenic on 11/11/2016.
 */
public class Packet implements Serializable {

	private Mark mark;
	private Payload payload;

	public static Packet create(Mark mark, Payload p) {
		return new Packet(mark, p);
	}

	public static Packet ack(Mark mark) {
		return new Packet(mark, null);
	}

	private Packet(Mark mark, Payload payload) {
		this.mark = mark;
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

	@Override
	public String toString() {
		return payload.toString();
	}

	public Mark getMark() {
		return mark;
	}

	public Payload getPayload() {
		return payload;
	}

}
