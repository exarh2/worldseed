package online.worldseed.generator.service.generator.model.option;

import lombok.Getter;

import java.util.Optional;

import static online.worldseed.generator.service.generator.model.TerrainGenerationType.TERRAIN_PLANET;

@Getter
public class PlanetTerrainOptions extends TerrainOptions {
    //Источник тестурирования
    private final String textureSource;

    public PlanetTerrainOptions(Double latStep, String textureSource) {
        super(TERRAIN_PLANET, latStep, Optional.empty());
        this.textureSource = textureSource;
    }
}
