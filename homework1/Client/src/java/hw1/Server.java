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

    public static void main(String[] args) {
        register("a", 0, 0, "b", 0);
        register("c", 1, 1, "d", 1);
        System.out.println(searchNeighbour("a"));
        System.out.println(storeMeasurement("a", "metan", 10));
    }

    public static boolean register(java.lang.String username, double latitude, double longitude, java.lang.String ipaddress, int port) {
        hw1.ServerService_Service service = new hw1.ServerService_Service();
        hw1.ServerService servport = service.getServerPort();
        return servport.register(username, latitude, longitude, ipaddress, port);
    }

    public static UserAddress searchNeighbour(java.lang.String username) {
        hw1.ServerService_Service service = new hw1.ServerService_Service();
        hw1.ServerService port = service.getServerPort();
        return port.searchNeighbour(username);
    }

    public static boolean storeMeasurement(java.lang.String username, java.lang.String parameter, float averageValue) {
        hw1.ServerService_Service service = new hw1.ServerService_Service();
        hw1.ServerService port = service.getServerPort();
        return port.storeMeasurement(username, parameter, averageValue);
    }

}
