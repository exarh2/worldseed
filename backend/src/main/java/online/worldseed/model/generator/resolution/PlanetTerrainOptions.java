package online.worldseed.model.generator.resolution;

import lombok.Getter;

import static online.worldseed.model.generator.TerrainType.TERRAIN_PLANET;

@Getter
public class PlanetTerrainOptions extends TerrainOptions {
    //Источник тестурирования
    private final String textureSource;

    public PlanetTerrainOptions(double latStep, double zoomFrom, String textureSource, TerrainCompression compression) {
        super(TERRAIN_PLANET, latStep, zoomFrom, compression);
        this.textureSource = textureSource;
    }
}
