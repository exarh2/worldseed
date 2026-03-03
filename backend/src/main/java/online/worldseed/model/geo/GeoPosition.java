package online.worldseed.model.geo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;

@Data
@AllArgsConstructor
public class GeoPosition {
    private double longitude;
    private double latitude;
    private double elevation;

    public GeoPosition(double longitude, double latitude) {
        this(longitude, latitude, Double.MIN_VALUE);
    }

    public GeoPosition(Point3 point) {
        this(point.getX(), point.getY(), point.getZ());
    }

    /**
     * В JTS: x = lon, y = lat, z = elevation
     */
    public Coordinate toCoordinate() {
        return new Coordinate(longitude, latitude, elevation);
    }
}
