/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hw1;

import java.io.Serializable;

/**
 *
 * @author fhrenic
 */
public class UserAddress implements Serializable {

    private String ipaddress;

    private int port;

    public UserAddress(String ipaddress, int port) {
        this.ipaddress = ipaddress;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAddress)) {
            return false;
        }

        UserAddress that = (UserAddress) o;

        if (port != that.port) {
            return false;
        }
        return ipaddress != null ? ipaddress.equals(that.ipaddress) : that.ipaddress == null;

    }

    @Override
    public int hashCode() {
        int result = ipaddress != null ? ipaddress.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    public String getIpaddress() {
        return ipaddress;
    }
    
    public void setIpaddress(String ipaddress){
        this.ipaddress = ipaddress;
    }

    public int getPort() {
        return port;
    }
    
    public void setPort(int port){
        this.port = port;
    }

}
