/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw1;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author fhrenic
 */
@WebService(portName = "ServerPort", serviceName = "ServerService", targetNamespace = "hw1")
public class ServerService {

    private final Map<String, SensorInfo> sensors = new HashMap<>();
    private final Map<String, List<Measurement>> measurements = new HashMap<>();

    private final List<String> log = new LinkedList<>();

    @WebMethod(operationName = "register")
    public boolean register(
            @WebParam(name = "username") String username,
            @WebParam(name = "latitude") double latitude,
            @WebParam(name = "longitude") double longitude,
            @WebParam(name = "ipaddress") String ipaddress,
            @WebParam(name = "port") int port
    ) {

        if (sensors.containsKey(username)) {
            return false;
        }

        sensors.put(username,
                new SensorInfo(
                        latitude, longitude,
                        new UserAddress(ipaddress, port)
                )
        );

        return true;
    }

    @WebMethod(operationName = "searchNeighbour")
    public UserAddress searchNeighbour(@WebParam(name = "username") String username) {
        if (username == null) {
            return null;
        }

        SensorInfo sensor = sensors.get(username);
        if (sensor == null) {
            return null;
        }

        SensorInfo neighbor = sensors.values()
                .parallelStream().unordered()
                .filter(s -> s != sensor)
                .min((s1, s2) -> Double.compare(sensor.distanceTo(s1), sensor.distanceTo(s2)))
                .orElse(sensor);
        if (sensor == neighbor || neighbor == null) {
            // there is only one sensor registered on the server and it's the
            // same one that called this method
            return null;
        }

        writeToLog("Closest sensor to %s is %.2lf away", username, sensor.distanceTo(neighbor));

        return neighbor.getAddress();
    }

    @WebMethod(operationName = "storeMeasurement")
    public boolean storeMeasurement(
            @WebParam(name = "username") String username,
            @WebParam(name = "parameter") String parameter,
            @WebParam(name = "averageValue") float averageValue
    ) {
        if (username == null || !sensors.containsKey(username)) {
            return false;
        }

        Measurement newEntry = new Measurement(parameter, averageValue);
        List<Measurement> list = measurements.get(username);
        if (list == null) {
            list = new LinkedList<>();
        }
        list.add(newEntry);
        measurements.put(username, list);

        writeToLog("Stored measurement for %s = %.2f, recieved from %s",
                parameter, averageValue, username);

        return true;
    }

    private void writeToLog(String fmt, Object... objects) {
        writeToLog(String.format(fmt, objects));
    }

    private void writeToLog(String message) {
        log.add(message);
        System.out.println(message);
    }

}
