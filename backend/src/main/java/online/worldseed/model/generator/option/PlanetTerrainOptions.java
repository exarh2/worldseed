package online.worldseed.model.generator.option;

import lombok.Getter;

import java.util.Optional;

import static online.worldseed.model.generator.TerrainGenerationType.TERRAIN_PLANET;

@Getter
public class PlanetTerrainOptions extends TerrainOptions {
    //Источник тестурирования
    private final String textureSource;

    public PlanetTerrainOptions(Double latStep, String textureSource) {
        super(TERRAIN_PLANET, latStep, Optional.empty());
        this.textureSource = textureSource;
    }
}
