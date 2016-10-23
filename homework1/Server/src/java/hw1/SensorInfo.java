package hw1;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by fhrenic on 19/10/2016.
 */
public class SensorInfo implements Serializable {

    private static final double R = 6371.0;

    private double longitude;

    private double latitude;

    private UserAddress address;

    public SensorInfo(double longitude, double latitude, UserAddress address) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }

    public double distanceTo(SensorInfo other) {
        if (other == null) {
            other = new SensorInfo(0, 0, null);
        }
        double dlon = other.longitude - longitude;
        double dlat = other.latitude - latitude;

        double dlat_sin = Math.sin(dlat / 2);
        double dlon_sin = Math.sin(dlon / 2);

        double x = dlat_sin * dlat_sin + Math.cos(latitude) * Math.cos(other.latitude) * dlon_sin * dlon_sin;

        return 2 * R * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 59 * hash + Objects.hashCode(this.address);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SensorInfo other = (SensorInfo) obj;
        if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        }
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        return true;
    }

    public UserAddress getAddress() {
        return address;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

}
