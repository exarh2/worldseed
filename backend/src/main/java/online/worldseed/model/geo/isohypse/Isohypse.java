package online.worldseed.model.geo.isohypse;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Isohypse {
    private double elevation;
    private List<Polyline> segments = new ArrayList<>();

    public Isohypse(double elevation) {
        this.elevation = elevation;
    }

    public void addSegment(Polyline isohypseSegment) {
        segments.add(isohypseSegment);
    }
}
