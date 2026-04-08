package online.worldseed.model.generator.resolution;

import lombok.Getter;

import static online.worldseed.model.generator.TerrainType.TERRAIN_OSM;

@Getter
public class OsmTerrainOptions extends TerrainOptions {
    //Максимальная видимость вокруг пользователя в террейнах,
    //Фронт может уменьшать, если машина слабая
    //Если террейнов нет, в казанном квадрате, то генерируются на 1 шире для более плавной подгрузки
    private final int maxTerrainViewDistance;

    public OsmTerrainOptions(double latStep, int maxTerrainViewDistance) {
        super(TERRAIN_OSM, latStep);
        this.maxTerrainViewDistance = maxTerrainViewDistance;
    }
}
