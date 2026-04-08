package online.worldseed.service.srtm;

import lombok.Data;
import lombok.Value;
import online.worldseed.model.exception.ServiceErrorException;
import online.worldseed.model.geo.GeoPosition;
import org.locationtech.jts.geom.Envelope;

import java.util.Arrays;

import static online.worldseed.model.srtm.StrmConstants.DEM3_RESOLUTION;

@Value
public class DigitalElevationModel {
    int west;
    int south;
    int width;
    int height;
    short[] data;
    Statistics statistics;

    public DigitalElevationModel(Envelope bounds) {
        var east = (int) (bounds.getMaxX() * DEM3_RESOLUTION + 1.5);
        var north = (int) (bounds.getMaxY() * DEM3_RESOLUTION + 1.5);

        west = (int) (bounds.getMinX() * DEM3_RESOLUTION + 1.5);
        south = (int) (bounds.getMinY() * DEM3_RESOLUTION + 1.5);

        width = east - west + 1;
        height = north - south + 1;

        data = new short[width * height];
        statistics = new Statistics();
    }

    public double getElevationForDataPoint(int localLon, int localLat) {
        short elevation16 = data[localLat * width + localLon];

        if (elevation16 != Short.MIN_VALUE) {
            return elevation16;
        }
        //Почему не 0?
        return Double.MIN_VALUE;
    }

    public void setElevationForDataPoint(int localLon, int localLat, short elevation) {
        if (elevation == Short.MIN_VALUE || elevation == Short.MAX_VALUE) {
            statistics.setMissingPoints(statistics.getMissingPoints() + 1);
        } else {
            if (statistics.getMinElevation() == null) {
                statistics.setMinElevation(elevation);
            } else if (elevation < statistics.getMinElevation()) {
                statistics.setMinElevation(elevation);
            }
            if (statistics.getMaxElevation() == null) {
                statistics.setMaxElevation(elevation);
            } else if (elevation > statistics.getMaxElevation()) {
                statistics.setMaxElevation(elevation);
            }
        }
        data[localLat * width + localLon] = elevation;
    }

    public GeoPosition getGeoPosition(double lonPos, double latPos, boolean calculateElevation) {
        GeoPosition pos = new GeoPosition(
            (west + lonPos - 0.5) / DEM3_RESOLUTION,
            (south + latPos - 0.5) / DEM3_RESOLUTION,
            calculateElevation ? calculateElevationForDataPoint(lonPos, latPos) : 0);
        return pos;
    }

    public double getAlt(double lon, double lat) {
        var lonPos = lon * DEM3_RESOLUTION - west + 0.5;
        var latPos = lat * DEM3_RESOLUTION - south + 0.5;
        var alt = calculateElevationForDataPoint(lonPos, latPos);
        if (alt < -1000) {
            throw new ServiceErrorException("Wrong calculated elevation for data point" + alt);
        }
        return alt;
    }

    private double calculateElevationForDataPoint(double localLon, double localLat) {
        int lonPos0 = (int) Math.floor(localLon);
        int lonPos1 = (int) Math.ceil(localLon);
        int latPos0 = (int) Math.floor(localLat);
        int latPos1 = (int) Math.ceil(localLat);

        if (lonPos0 < 0 || lonPos1 >= this.width || latPos0 < 0 || latPos1 >= this.height) {
            return Double.MIN_VALUE;
        }
        // get elevations of adjacent known elevation points
        double[] elevations = new double[]{
            getElevationForDataPoint(lonPos0, latPos0),
            getElevationForDataPoint(lonPos1, latPos0),
            getElevationForDataPoint(lonPos0, latPos1),
            getElevationForDataPoint(lonPos1, latPos1)};
        //Если нет данных - усредняем оставшиеся , пока так чтобы швов не было
        var avg = Arrays.stream(elevations).filter(e -> e != Double.MIN_VALUE).average().orElse(Double.MIN_VALUE);

        double elev1 = orElse(elevations[0], avg) + (orElse(elevations[1], avg) - orElse(elevations[0], avg)) * (localLon - lonPos0);
        double elev2 = orElse(elevations[2], avg) + (orElse(elevations[3], avg) - orElse(elevations[2], avg)) * (localLon - lonPos0);
        return elev1 + (elev2 - elev1) * (localLat - latPos0);
    }

    private double orElse(double elevation, double avg) {
        if (elevation == Double.MIN_VALUE) {
            return avg;
        }
        return elevation;
    }

    @Data
    public class Statistics {
        Short minElevation;
        Short maxElevation;
        Integer missingPoints = 0;

        public Boolean hasMissingPoints() {
            return missingPoints > 0;
        }
    }
}
