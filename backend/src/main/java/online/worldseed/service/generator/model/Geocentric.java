package online.worldseed.generator.service.generator.model;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.data.util.Pair;

import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static online.worldseed.generator.service.generator.model.GeoConstants.EARTH_RADIUS_A;
import static online.worldseed.generator.service.generator.model.GeoConstants.EARTH_RADIUS_B;

/**
 * Пространственная координата
 * Created by akudryashov on 04.06.2015.
 *
 * @note Геоцентрические декартовы прямоугольные координаты:
 * начало координат находится в центре эллипсоида,
 * ось z расположена вдоль оси вращения эллипсоида и направлена в северный полюс,
 * ось x лежит в пересечении экватора и начального меридиана,
 * ось y лежит в пересечении экватора и меридиана с долготой L = 90°.
 * @see <a href="http://lnfm1.sai.msu.ru/grav/russian/lecture/tfe/node3.html">Вычисление геодезических координат</a>
 * @see <a href="http://gis-lab.info/qa/geodesic-coords.html#sel=60:1,62:13;63:1,69:5">Геодезические системы пространственных координат</a>
 */
@Getter
@Setter
public class Geocentric {
    double x;
    double y;
    double z;

    Optional<Vector3D> normal;
    Optional<Pair<Float, Float>> uv;

    public Geocentric(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Geocentric(double x, double y, double z, Optional<Vector3D> normal, Optional<Pair<Float, Float>> uv) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.normal = normal;
        this.uv = uv;
    }

    /**
     * Вычисление расстояния от текущей координаты до указанной
     */
    public double distance(Geocentric gcTo) {
        return sqrt((x - gcTo.x) * (x - gcTo.x) + (y - gcTo.y) * (y - gcTo.y) + (z - gcTo.z) * (z - gcTo.z));
    }

    public RealVector toRealVector() {
        return new ArrayRealVector(new double[]{x, y, z, 1}, false);
    }

    public Vector3D toVector3D() {
        return new Vector3D(x, y, z);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Geocentric that = (Geocentric) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    /**
     * Создание геоцентрическую координату из геодезической
     */
    public static Geocentric fromGeodetic(Geodetic geodetic) {
        val altitude = geodetic.getAlt();
        val cosB = cos(toRadians(geodetic.getLat()));
        val sinB = sin(toRadians(geodetic.getLat()));
        val p = (EARTH_RADIUS_A * EARTH_RADIUS_A) /
                sqrt((EARTH_RADIUS_A * EARTH_RADIUS_A) * (cosB * cosB) + (EARTH_RADIUS_B * EARTH_RADIUS_B) * (sinB * sinB));
        val x = (p + altitude) * cosB * cos(toRadians(geodetic.getLon()));
        val y = (p + altitude) * cosB * sin(toRadians(geodetic.getLon()));
        val z = (((EARTH_RADIUS_B * EARTH_RADIUS_B) / (EARTH_RADIUS_A * EARTH_RADIUS_A)) * p + altitude) * sinB;
        return new Geocentric(x, y, z);
    }

    /**
     * Создание геоцентрическую координату из геодезической координаты
     */
    public static Geocentric fromGeodeticCoordinate(Coordinate coordinate) {
        return fromGeodetic(new Geodetic(coordinate.getY(), coordinate.getX(),
                Double.isNaN(coordinate.getZ()) ? 0 : coordinate.getZ()));
    }
}

