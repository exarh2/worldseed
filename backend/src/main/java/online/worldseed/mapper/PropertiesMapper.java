package online.worldseed.mapper;

import online.worldseed.config.properties.GeneratorProperties;
import online.worldseed.model.generator.TerrainType;
import online.worldseed.model.generator.resolution.AltitudeTerrainOptions;
import online.worldseed.model.generator.resolution.OsmTerrainOptions;
import online.worldseed.model.generator.resolution.PlanetTerrainOptions;
import online.worldseed.model.generator.resolution.TerrainOptions;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface PropertiesMapper {
    default TerrainOptions toTerrainOptions(GeneratorProperties.ResolutionProperties source) {
        var generationType = source.getGenerationType();
        if (generationType == TerrainType.TERRAIN_OSM) {
            return toOsmTerrainOptions(source);
        }
        if (generationType == TerrainType.TERRAIN_ALTITUDE) {
            return toAltitudeTerrainOptions(source);
        }
        if (generationType == TerrainType.TERRAIN_PLANET) {
            return toPlanetTerrainOptions(source);
        }
        throw new IllegalArgumentException("Unsupported generationType: " + generationType);
    }

    OsmTerrainOptions toOsmTerrainOptions(GeneratorProperties.ResolutionProperties source);

    AltitudeTerrainOptions toAltitudeTerrainOptions(GeneratorProperties.ResolutionProperties source);

    PlanetTerrainOptions toPlanetTerrainOptions(GeneratorProperties.ResolutionProperties source);
}
