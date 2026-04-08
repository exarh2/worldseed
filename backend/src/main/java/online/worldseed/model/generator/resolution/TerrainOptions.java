package online.worldseed.model.generator.resolution;

import lombok.Getter;
import online.worldseed.model.generator.TerrainType;

/**
 * Кастомные настройки генерации и показа террейнов
 * TODO https://wiki.openstreetmap.org/wiki/Zoom_levels
 */
@Getter
public abstract class TerrainOptions {
    //Вид террейна (зависит зависимости от разрешения)
    protected TerrainType generationType;

    //Шаг нарезки сетки по широте
    protected double latStep;

    //Целевой OSM zoom для текущего шага нарезки
    protected double zoomTo;

    protected TerrainOptions(TerrainType generationType, double latStep, double zoomTo) {
        this.generationType = generationType;
        this.latStep = latStep;
        this.zoomTo = zoomTo;
    }
}
