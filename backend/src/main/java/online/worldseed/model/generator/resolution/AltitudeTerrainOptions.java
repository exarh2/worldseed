package online.worldseed.model.generator.resolution;

import lombok.Getter;

import static online.worldseed.model.generator.TerrainType.TERRAIN_ALTITUDE;

@Getter
public class AltitudeTerrainOptions extends TerrainOptions {
    //Максимальная видимость вокруг пользователя в террейнах,
    //Фронт может уменьшать, если машина слабая
    //Если террейнов нет, в казанном квадрате, то генерируются на 1 шире для более плавной подгрузки
    private final int maxTerrainViewDistance;

    //Размер сетки зависит от DEM3_RESOLUTION и размера полигона
    //Максимально, например, для 1/64 можно получить DEM3_RESOLUTION/64 = 18.75,
    //а дальше разбивать бесполезно, т.к. высотные данные просто экстраполируются
    private final int gridSize;

    public AltitudeTerrainOptions(double latStep, double zoomTo, int maxTerrainViewDistance, int gridSize) {
        super(TERRAIN_ALTITUDE, latStep, zoomTo);
        this.maxTerrainViewDistance = maxTerrainViewDistance;
        this.gridSize = gridSize;
    }
}
