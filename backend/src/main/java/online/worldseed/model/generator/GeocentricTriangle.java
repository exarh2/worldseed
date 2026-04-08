package online.worldseed.model.generator;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeocentricTriangle {
    private List<Geocentric> gcCoordinates;
    private boolean temporal;
}
