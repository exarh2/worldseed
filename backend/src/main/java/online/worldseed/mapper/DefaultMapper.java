package online.worldseed.mapper;

import online.worldseed.model.dto.scene.resolution.AltitudeTerrainOptionsDto;
import online.worldseed.model.dto.scene.resolution.OsmTerrainOptionsDto;
import online.worldseed.model.dto.scene.resolution.PlanetTerrainOptionsDto;
import online.worldseed.model.dto.scene.resolution.TerrainOptionsDto;
import online.worldseed.model.generator.resolution.AltitudeTerrainOptions;
import online.worldseed.model.generator.resolution.OsmTerrainOptions;
import online.worldseed.model.generator.resolution.PlanetTerrainOptions;
import online.worldseed.model.generator.resolution.Resolution;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface DefaultMapper {
    default TerrainOptionsDto toTerrainOptionsDto(Resolution resolution) {
        var terrainOptions = resolution.getTerrainOptions();
        if (terrainOptions instanceof OsmTerrainOptions osm) {
            return new OsmTerrainOptionsDto(resolution, osm.getLatStep(), osm.getMaxTerrainViewDistance());
        }
        if (terrainOptions instanceof AltitudeTerrainOptions altitude) {
            return new AltitudeTerrainOptionsDto(
                resolution,
                altitude.getLatStep(),
                altitude.getMaxTerrainViewDistance()
            );
        }
        if (terrainOptions instanceof PlanetTerrainOptions planet) {
            return new PlanetTerrainOptionsDto(resolution, planet.getLatStep());
        }
        throw new IllegalArgumentException("Unsupported terrainOptions type: " + terrainOptions.getClass().getName());
    }
}
