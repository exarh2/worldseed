package online.worldseed.model.generator;

import lombok.Value;
import org.locationtech.jts.geom.Coordinate;

import java.util.Comparator;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static online.worldseed.model.generator.GeoConstants.EARTH_RADIUS_A;
import static online.worldseed.model.generator.GeoConstants.EARTH_RADIUS_B;
import static online.worldseed.model.generator.GeoConstants.F;
import static online.worldseed.model.generator.GeoConstants.FIRST_ECCENTRICITY;
import static online.worldseed.model.generator.GeoConstants.POLAR_RADIUS_OF_CURVATURE;
import static online.worldseed.model.generator.GeoConstants.SECOND_ECCENTRICITY;

/**
 * Геодезическая координата (широта/долгота/высота) GeodeticCoordinate
 * Created by akudryashov on 16.05.2014.
 *
 * @note Система геодезических координат:
 * геодезическая широта B (Geodetic latitude)
 * угол между нормалью к поверхности эллипсоида и плоскостью экватора,
 * геодезическая долгота L (Geodetic longitude)
 * угол между плоскостями данного и начального меридианов,
 * геодезическая высота H (geodesic altitude)
 * кратчайшее расстояние до поверхности эллипсоида.
 * @see <a href="http://lnfm1.sai.msu.ru/grav/russian/lecture/tfe/node3.html">Вычисление геодезических координат</a>
 * @see <a href="http://gis-lab.info/qa/geodesic-coords.html#sel=60:1,62:13;63:1,69:5">Геодезические системы пространственных координат</a>
 */
@Value
public class Geodetic implements Comparable<Geodetic> {
    double lat;
    double lon;
    double alt;

    public Geodetic(double lat, double lon, double alt) {
        assert lat >= -90 && lat <= 90;
        assert lon >= -180 && lon <= 180;

        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    public Geodetic(double lat, double lon) {
        this(lat, lon, 0);
    }

    @Override
    public int compareTo(Geodetic geodetic) {
        return Comparator.comparing(Geodetic::getLat)
                .thenComparing(Geodetic::getLon)
                .compare(this, geodetic);
    }

    /**
     * Создать новую геодезическую координату с высотой на alt больше текущей
     */
    public Geodetic addAlt(double alt) {
        return new Geodetic(lat, lon, this.alt + alt);
    }

    /**
     * Вычисление расстояния от текущей координаты до указанной на плоскости (высота не учитывается)
     */
    public double planeDistance(Geodetic gdTo) {
        return sqrt((lat - gdTo.lat) * (lat - gdTo.lat) + (lon - gdTo.lon) * (lon - gdTo.lon));
    }

    public Coordinate toCoordinate() {
        return new Coordinate(lon, lat);
    }

    public Geocentric toGeocentric() {
        return Geocentric.fromGeodetic(this);
    }

    @Override
    public String toString() {
        return "[" + lat + ", " + lon + ", H = " + alt + "]";
    }

    //    /**
    //     * Получение геодезической координаты по координаты JTS
    //     */
    //    public static Geodetic fromCoordinate(Coordinate coordinate) {
    //        return new Geodetic(coordinate.getX(), coordinate.getY(), coordinate.getZ());
    //    }

    /**
     * Получение геодезической координаты по геоцентрической
     */
    public static Geodetic fromGeocentric(Geocentric gc) {
        var p = hypot(gc.getX(), gc.getY());
        if (p == 0) {
            var lat = gc.getZ() >= 0 ? PI / 2 : -PI / 2;
            var lon = 0;
            var h = abs(gc.getZ()) - EARTH_RADIUS_B;
            return new Geodetic(toDegrees(lat), toDegrees(lon), h);
        } else {
            var t = gc.getZ() / p * (1 + (SECOND_ECCENTRICITY * EARTH_RADIUS_B) / hypot(p, gc.getZ()));
            // Как правило, бывает достаточно одной итерации, но можно больше
            for (int i = 1; i <= 1; i++) {
                t = t * (1 - F);
                var lat = atan(t);
                var cosLat = cos(lat);
                var sinLat = sin(lat);
                t = (gc.getZ() + SECOND_ECCENTRICITY * EARTH_RADIUS_B * pow(sinLat, 3)) /
                        (p - FIRST_ECCENTRICITY * EARTH_RADIUS_A * pow(cosLat, 3));
            }
            var lon = atan2(gc.getY(), gc.getX());
            var lat = atan(t);
            var cosLat = cos(lat);
            var n = POLAR_RADIUS_OF_CURVATURE / sqrt(1 + SECOND_ECCENTRICITY * pow(cosLat, 2));
            var h = abs(t) <= 1 ? (p / cosLat - n) :
                    (gc.getZ() / sin(lat) - n * (1 - FIRST_ECCENTRICITY));
            return new Geodetic(toDegrees(lat), toDegrees(lon), h);
        }
    }
}
