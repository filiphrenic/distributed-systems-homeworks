/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw1;

/**
 *
 * @author fhrenic
 */
public class Server {
    
    private static final hw1.ServerService service;
    static {
        service = new hw1.ServerService_Service().getServerPort();
    }

    public static boolean register(java.lang.String username, double latitude, double longitude, java.lang.String ipaddress, int port) {
        return service.register(username, latitude, longitude, ipaddress, port);
    }

    public static UserAddress searchNeighbour(java.lang.String username) {
        return service.searchNeighbour(username);
    }

    public static boolean storeMeasurement(java.lang.String username, java.lang.String parameter, float averageValue) {
        return service.storeMeasurement(username, parameter, averageValue);
    }

}
