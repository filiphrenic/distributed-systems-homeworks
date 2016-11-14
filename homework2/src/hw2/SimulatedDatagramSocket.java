/*
 * This code has been developed at Department of Telecommunications,
 * Faculty of Electrical Engineering and Computing, University of Zagreb.
 */
package hw2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class SimulatedDatagramSocket extends DatagramSocket {

	private final double lossRate;
	private final int averageDelay;

	static DatagramSocket client(double lossRate, int averageDelay)
			throws SocketException {
		return new SimulatedDatagramSocket(0, lossRate, averageDelay, 0);
	}

	static DatagramSocket server(int port, double lossRate, int averageDelay)
			throws SocketException {
		return new SimulatedDatagramSocket(port, lossRate, averageDelay, 4 * averageDelay);
	}

	private SimulatedDatagramSocket(int port, double lossRate, int averageDelay, int soTimeout)
			throws SocketException {
		super(port);
		this.lossRate = lossRate;
		this.averageDelay = averageDelay;
		super.setSoTimeout(soTimeout);
	}

	@Override
	synchronized public void send(DatagramPacket packet) throws IOException {

		if (Util.RANDOM.nextDouble() >= lossRate) {

			//delay is uniformly distributed between 0 and 2*averageDelay
			long delay = (long) (2 * averageDelay * Util.RANDOM.nextDouble());

			new Thread(() -> {
				try {
					TimeUnit.MILLISECONDS.sleep(delay);
					SimulatedDatagramSocket.super.send(packet);
				} catch (InterruptedException e) {
					Thread.interrupted();
				} catch (IOException ex) {
					Logger.getLogger(SimulatedDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			).start();

		} else {
			Util.debug("Dropped packet...");
		}
	}
}
