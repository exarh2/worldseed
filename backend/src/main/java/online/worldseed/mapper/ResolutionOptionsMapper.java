package online.worldseed.mapper;

import online.worldseed.config.properties.GeneratorProperties;
import online.worldseed.model.generator.TerrainType;
import online.worldseed.model.generator.option.AltitudeTerrainOptions;
import online.worldseed.model.generator.option.OsmTerrainOptions;
import online.worldseed.model.generator.option.PlanetTerrainOptions;
import online.worldseed.model.generator.option.TerrainOptions;
import org.mapstruct.Mapper;

import java.util.Objects;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ResolutionOptionsMapper {
    default TerrainOptions toTerrainOptions(GeneratorProperties.ResolutionProperties source) {
        var generationType = source.getGenerationType();
        if (generationType == TerrainType.TERRAIN_OSM) {
            return new OsmTerrainOptions(
                    source.getLatStep(),
                    Objects.requireNonNull(source.getMaxTerrainViewDistance(),
                            "maxTerrainViewDistance is required for TERRAIN_OSM")
            );
        }
        if (generationType == TerrainType.TERRAIN_ALTITUDE) {
            return new AltitudeTerrainOptions(
                    source.getLatStep(),
                    Objects.requireNonNull(source.getMaxTerrainViewDistance(),
                            "maxTerrainViewDistance is required for TERRAIN_ALTITUDE"),
                    Objects.requireNonNull(source.getGridSize(),
                            "gridSize is required for TERRAIN_ALTITUDE")
            );
        }
        if (generationType == TerrainType.TERRAIN_PLANET) {
            return new PlanetTerrainOptions(
                    source.getLatStep(),
                    Objects.requireNonNull(source.getTextureSource(),
                            "textureSource is required for TERRAIN_PLANET")
            );
        }
        throw new IllegalArgumentException("Unsupported generationType: " + generationType);
    }
}
