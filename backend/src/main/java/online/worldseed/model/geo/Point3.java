package online.worldseed.model.geo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Point3 {
    private double x;
    private double y;
    private double z;

    public boolean isEquals(Point3 otherPoint) {
        if (otherPoint == null) {
            return false;
        }
        return x == otherPoint.x && y == otherPoint.y && z == otherPoint.z;
    }

    /**
     * "Почти равен" с допуском eps
     */
    public boolean equalsWithTolerance(Point3 other, double eps) {
        return Math.abs(this.x - other.x) <= eps && Math.abs(this.y - other.y) <= eps && Math.abs(this.z - other.z) <= eps;
    }
}
