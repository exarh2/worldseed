package online.worldseed.service.generator.model.option;

import lombok.Getter;

import java.util.Optional;

import static online.worldseed.service.generator.model.TerrainGenerationType.TERRAIN_ALTITUDE;

@Getter
public class AltitudeTerrainOptions extends TerrainOptions {
    //Размер сетки зависит от DEM3_RESOLUTION и размера полигона
    //Максимально, например, для 1/64 можно получить DEM3_RESOLUTION/64 = 18.75,
    //а дальше разбивать бесполезно, т.к. высотные данные просто экстраполируются
    private final Integer gridSize;

    public AltitudeTerrainOptions(Double latStep, int maxTerrainViewDistance, Integer gridSize) {
        super(TERRAIN_ALTITUDE, latStep, Optional.of(maxTerrainViewDistance));
        this.gridSize = gridSize;
    }
}
