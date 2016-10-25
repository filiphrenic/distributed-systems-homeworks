package hw1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.xml.ws.WebServiceException;

/**
 * Created by fhrenic on 20/10/2016.
 */
public class Sensor {

    private static int ID = 0;
    private static final int PORT = 20000;
    private static final String MEASUREMENTS_FILENAME = "mjerenja.csv";

    private int id;
    private final long startSeconds;
    private String[] header;
    private List<String> measurements;

    private AtomicBoolean running;
    private AtomicBoolean listening;

    public static void main(String[] args) throws IOException, InterruptedException {
        Sensor s1 = new Sensor();
        s1.listen();
        Sensor s2 = new Sensor();
        TimeUnit.SECONDS.sleep(4);
        s2.run();

//        s1.stopListening();
//        s2.stopRunning();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(reader.readLine());
    }

    public Sensor() throws IllegalStateException {
        Util.debug("Creating sensor...");
        startSeconds = Util.currentTimeSeconds();
        readMeasurements();
        Util.debug("Read measurements");

        int numberOfAttempts = 5;
        for (int i = 0; i < numberOfAttempts; i++) {
            id = ++Sensor.ID;

            try {
                if (Server.register(
                        getUsername(),
                        Util.randomLatitude(), Util.randomLongitude(),
                        Util.getLocalIP(), getPort()
                )) {
                    break;
                }
                id = -1;

            } catch (WebServiceException e) {
                throw new IllegalStateException("Couldn't connect to web service", e);
            }
        }
        if (id == -1) {
            throw new IllegalStateException(
                    String.format("Couldn't register to server in %d attempts", numberOfAttempts)
            );
        }

        Util.debug("%s registered", getUsername());

        running = new AtomicBoolean(false);
        listening = new AtomicBoolean(false);

        Util.debug("Successfully created sensor %s", getUsername());
    }

    public final String getUsername() {
        return "sensor" + id;
    }

    public final int getPort() {
        return PORT + id;
    }

    public boolean isListening() {
        return listening.get();
    }

    public boolean listen() {
        if (listening.get()) {
            Util.debug("%s already listening", getUsername());
            return false;
        }
        listening.set(true);
        new Thread(this.new Listener()).start();
        Util.debug("%s started listening", getUsername());
        return true;
    }

    public boolean stopListening() {
        Util.debug("%s stop listening", getUsername());
        return listening.compareAndSet(true, false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean run() {
        if (running.get()) {
            Util.debug("%s already running", getUsername());
            return false;
        }
        running.set(true);
        new Thread(this.new MeasurementRequester()).start();
        Util.debug("%s started running", getUsername());
        return true;
    }

    public boolean stopRunning() {
        Util.debug("%s stop running", getUsername());
        return running.compareAndSet(true, false);
    }

    private void log(String s) {
        System.out.println(s);
    }

    private void log(String fmt, Object... args) {
        log(String.format(fmt, args));
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

            int port = getPort();

            try (ServerSocket serverSocket = new ServerSocket(port);) {

                // set timeout to check if sensor is still listening
                serverSocket.setSoTimeout(5000);

                Util.debug("%s listening on port %d", getUsername(), port);

                while (Sensor.this.listening.get()) {
                    Util.debug("%s waiting for client", getUsername());
                    try {
                        Socket clientSocket = serverSocket.accept();
                        Util.debug("%s => client connected", getUsername());
                        executor.execute(new MeasurementProvider(clientSocket));
                    } catch (SocketTimeoutException ste) {
                        Util.except(ste);
                        if (!Sensor.this.listening.get()) {
                            break;
                        }
                    } catch (IOException e) {
                        Util.except(e);
                        break;
                    }
                }

            } catch (IOException e) {
                Util.except(e);
            } finally {
                Util.debug("Closing listeners");
                executor.shutdown();
            }

            Sensor.this.listening.set(false); // just to be sure (in case of exceptions)
        }

    }

    /**
     * Provides new measurements to the given socket.
     */
    private class MeasurementProvider implements Runnable {

        private final Socket client;

        MeasurementProvider(Socket client) {
            this.client = client;
            Util.debug("Created measurement provider for %s", getUsername());
        }

        @Override
        public void run() {

            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8)
                    );
                    PrintWriter writer = new PrintWriter(client.getOutputStream(), true);) {

                while (Sensor.this.listening.get()) {

                    String request = reader.readLine();

                    Util.debug("%s recieved <%s>", getUsername(), request);
                    if ("GET".equalsIgnoreCase(request)) {
                        String measurement = Sensor.this.getRandomMeasurement();
                        Util.debug("Sending measurements %s", measurement);
                        writer.println(measurement);
                    } else if ("QUIT".equalsIgnoreCase(request)) {
                        Util.debug("%s recieved quit");
                        break;
                    } else {
                        break;
                    }
                }

            } catch (Exception e) {
                Util.except(e);
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            Util.debug("%s quit providing measurements", getUsername());
            log("%s quit providing measurements", getUsername());

        }

    }

    /**
     * Requests new measurements to the given socket.
     */
    private class MeasurementRequester implements Runnable {

        @Override
        public void run() {

            String username = getUsername();
            UserAddress closest = Server.searchNeighbour(username);

            if (closest == null) {
                Util.debug("There is no sensor close to %s", username);
                return;
            }

            Util.debug("%s found closest sensor at %s:%d", username, closest.ipaddress, closest.port);

            try (Socket sensorSocket = new Socket(closest.getIpaddress(), closest.getPort())) {

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(sensorSocket.getInputStream(), StandardCharsets.UTF_8)
                );
                PrintWriter writer = new PrintWriter(sensorSocket.getOutputStream(), true);

                while (Sensor.this.running.get()) {

                    Util.debug("%s getting measurements...");
                    String myMeasurements = Sensor.this.getRandomMeasurement();

                    Util.debug("%s requesting measurements from peer", getUsername());
                    writer.println("GET");

                    String measurements = reader.readLine();
                    if (measurements == null) {
                        break;
                    }
                    measurements = measurements.trim();

                    Util.debug("%s sending measurements to server", getUsername());
                    sendToServer(myMeasurements, measurements);

                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }

                Util.debug("%s sending QUIT to peer sensor", getUsername());
                writer.println("QUIT");

            } catch (IOException e) {
                Util.except(e);
            }

            Sensor.this.running.set(false); // just to be sure (in case of exceptions)
        }

        /**
         * Calculate averages and send measurements to server
         *
         * @param m1 measurements as string
         * @param m2 measurements as string
         */
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

                if (Server.storeMeasurement(getUsername(), Sensor.this.header[i], average)) {
                    Util.debug("Successfully stored measurements from %s", getUsername());
                } else {
                    Util.debug("Failed storing measurements from %s", getUsername());
                }
            }
        }

    }

    // MISC
    private long activeSeconds() {
        return Util.currentTimeSeconds() - startSeconds;
    }

    private String getRandomMeasurement() {
        long seconds = activeSeconds();
        long index = seconds % measurements.size();
        String measurement = measurements.get((int) index);

        log("%s@%d getting measurements [%s] at index %d", getUsername(), seconds, measurement, index);

        return measurement;
    }

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

}
