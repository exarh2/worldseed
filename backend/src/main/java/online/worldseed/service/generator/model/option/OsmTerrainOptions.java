package online.worldseed.service.generator.model.option;

import lombok.Getter;

import java.util.Optional;

import static online.worldseed.service.generator.model.TerrainGenerationType.TERRAIN_OSM;

@Getter
public class OsmTerrainOptions extends TerrainOptions {
    public OsmTerrainOptions(Double latStep, int maxTerrainViewDistance) {
        super(TERRAIN_OSM, latStep, Optional.of(maxTerrainViewDistance));
    }
}
