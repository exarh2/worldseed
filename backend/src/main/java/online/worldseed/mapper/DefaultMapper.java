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
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface DefaultMapper {
    default TerrainOptionsDto toTerrainOptionsDto(Resolution resolution) {
        var terrainOptions = resolution.getTerrainOptions();
        if (terrainOptions instanceof OsmTerrainOptions osm) {
            return toOsmTerrainOptionsDto(osm, resolution);
        }
        if (terrainOptions instanceof AltitudeTerrainOptions altitude) {
            return toAltitudeTerrainOptionsDto(altitude, resolution);
        }
        if (terrainOptions instanceof PlanetTerrainOptions planet) {
            return toPlanetTerrainOptionsDto(planet, resolution);
        }
        throw new IllegalArgumentException("Unsupported terrainOptions type: " + terrainOptions.getClass().getName());
    }

    @Mapping(target = "resolution", source = "resolution")
    OsmTerrainOptionsDto toOsmTerrainOptionsDto(OsmTerrainOptions source, Resolution resolution);

    @Mapping(target = "resolution", source = "resolution")
    AltitudeTerrainOptionsDto toAltitudeTerrainOptionsDto(AltitudeTerrainOptions source, Resolution resolution);

    @Mapping(target = "resolution", source = "resolution")
    PlanetTerrainOptionsDto toPlanetTerrainOptionsDto(PlanetTerrainOptions source, Resolution resolution);
}
