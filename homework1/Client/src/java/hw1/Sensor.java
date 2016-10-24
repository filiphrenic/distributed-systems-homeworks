package hw1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.xml.ws.WebServiceException;

/**
 * Created by fhrenic on 20/10/2016.
 */
public class Sensor {

    private static final int PORT = 55555;
    private static final String MEASUREMENTS_FILENAME = "mjerenja.csv";

    private String username;
    private final long startSeconds;
    private String[] header;
    private List<String> measurements;

    private AtomicBoolean running;
    private AtomicBoolean listening;

    public static void main(String[] args) {
        new Sensor();
    }

    public Sensor() throws IllegalStateException {
        startSeconds = Util.currentTimeSeconds();
        readMeasurements();

        int numberOfAttempts = 5;
        for (int i = 0; i < numberOfAttempts; i++) {
            username = ""; // TODO get username from somewhere

            try {
                if (Server.register(
                        username,
                        Util.randomLatitude(), Util.randomLongitude(),
                        Util.getLocalIP(), PORT
                )) {
                    break;
                }
                username = null;

            } catch (WebServiceException e) {
                throw new IllegalStateException("Couldn't connect to web service", e);
            }
        }
        if (username == null) {
            throw new IllegalStateException(
                    String.format("Couldn't register to server in %d attempts", numberOfAttempts)
            );
        }

    }

    public String getUsername() {
        return username;
    }

    public boolean isListening() {
        return listening.get();
    }

    public boolean listen() {
        if (listening.get()) {
            return false;
        }
        listening.set(true);
        new Thread(this.new Listener()).start();
        return true;
    }

    public boolean stopListening() {
        return listening.compareAndSet(true, false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean run() {
        if (running.get()) {
            return false;
        }
        running.set(true);
        new Thread(this.new MeasurementRequester()).start();
        return true;
    }

    public boolean stopRunning() {
        return running.compareAndSet(true, false);
    }

    /**
     * Listens to incoming connections and handles them
     */
    private class Listener implements Runnable {

        private final ExecutorService executor;

        Listener() {
            executor = Executors.newCachedThreadPool();
        }

        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(Sensor.PORT);) {

                // set timeout to avoid blocking
                serverSocket.setSoTimeout(500);

                while (Sensor.this.running.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executor.execute(new MeasurementProvider(clientSocket));
                    } catch (SocketTimeoutException ste) {
                        // do nothing, check runningFlag flag
                    } catch (IOException e) {
                        Util.except(e);
                        break;
                    }
                }

            } catch (IOException e) {
                Util.except(e);
            } finally {
                executor.shutdown();
            }
        }

    }

    /**
     * Provides new measurements to the given socket.
     */
    private class MeasurementProvider implements Runnable {

        private final Socket client;

        MeasurementProvider(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)
                    );
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)
                    );) {

                while (Sensor.this.listening.get()) {
                    String request = reader.readLine();
                    if ("GET".equalsIgnoreCase(request)) {
                        writer.write(Sensor.this.getRandomMeasurement());
                    } else if ("QUIT".equalsIgnoreCase(request)) {
                        break;
                    }
                }

            } catch (Exception e) {

            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    // ignore
                }
            }

        }

    }

    /**
     * Requests new measurements to the given socket.
     */
    private class MeasurementRequester implements Runnable {

        @Override
        public void run() {

            UserAddress closest = Server.searchNeighbour(Sensor.this.username);

            try (Socket sensorSocket = new Socket(closest.getIpaddress(), closest.getPort())) {

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(sensorSocket.getInputStream(), StandardCharsets.UTF_8)
                );
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(sensorSocket.getOutputStream(), StandardCharsets.UTF_8)
                );

                while (Sensor.this.running.get()) {
                    String myMeasurements = Sensor.this.getRandomMeasurement();
                    writer.write("GET");
                    String measurements = reader.readLine();
                    sendToServer(myMeasurements, measurements);
                }

                writer.write("QUIT");

            } catch (IOException e) {
                Util.except(e);
            }
        }

        private void sendToServer(String m1, String m2) {
            List<Float> my = Util.stringToFloats(m1);
            List<Float> his = Util.stringToFloats(m2);

            int size = Math.min(my.size(), his.size());
            size = Math.min(size, Sensor.this.header.length);

            for (int i = 0; i < size; i++) {
                float average = my.get(i);
                if (average == 0.0f) {
                    continue;
                }
                float m_his = his.get(i);
                if (m_his != 0) {
                    average = (average + m_his) / 2.0f;
                }

                Server.storeMeasurement(Sensor.this.username, Sensor.this.header[i], average);
            }
        }

    }

    // MISC
    private void readMeasurements() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/" + MEASUREMENTS_FILENAME), StandardCharsets.UTF_8
                )
        )) {
            header = reader.readLine().split(",");
            measurements = reader.lines().collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            Util.except(e);
            System.exit(1);
        }
    }

    private long activeSeconds() {
        return Util.currentTimeSeconds() - startSeconds;
    }

    private String getRandomMeasurement() {
        long index = activeSeconds() % 100;
        return measurements.get((int) index);
    }

}
