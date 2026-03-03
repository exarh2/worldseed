package online.worldseed.model.geo.isohypse;

import lombok.Getter;
import online.worldseed.model.geo.Point3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Polyline {
    private List<Point3> vertices;
    private boolean closed;

    public Polyline(boolean closed) {
        this.closed = closed;
        this.vertices = new ArrayList<>();
    }

    public void addVertex(Point3 point) {
        vertices.add(point);
    }

    public void reverse() {
        Collections.reverse(vertices);
    }

    public void removeDuplicateVertices() {
        for (int i = 0; i < vertices.size() && vertices.size() > 1; ) {
            var point1 = vertices.get(i);
            int j = (i + 1) % vertices.size();

            var point2 = vertices.get(j);

            if (point2.isEquals(point1)) {
                vertices.remove(j);
            } else {
                i++;
            }
        }
    }
}
