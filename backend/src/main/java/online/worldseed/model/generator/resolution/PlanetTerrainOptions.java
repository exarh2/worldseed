package online.worldseed.model.generator.resolution;

import lombok.Getter;

import static online.worldseed.model.generator.TerrainType.TERRAIN_PLANET;

@Getter
public class PlanetTerrainOptions extends TerrainOptions {
    //Источник тестурирования
    private final String textureSource;

    public PlanetTerrainOptions(double latStep, double zoomTo, String textureSource) {
        super(TERRAIN_PLANET, latStep, zoomTo);
        this.textureSource = textureSource;
    }
}
