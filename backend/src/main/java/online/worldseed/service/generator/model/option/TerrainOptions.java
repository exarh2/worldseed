package online.worldseed.generator.service.generator.model.option;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.worldseed.generator.service.generator.model.TerrainGenerationType;

import java.util.Optional;

import static online.worldseed.generator.service.generator.model.GeoConstants.LATITUDE_ONE_DEGREE_IN_METERS;

/**
 * Кастомные настройки генерации и показа террейнов
 * TODO https://wiki.openstreetmap.org/wiki/Zoom_levels
 */
@Getter
@AllArgsConstructor
public abstract class TerrainOptions {
    //Вид террейна (зависит зависимости от разрешения)
    protected TerrainGenerationType generationType;

    //Шаг нарезки сетки по широте
    protected Double latStep;

    //Максимальная видимость вокруг пользователя в террейнах,
    //Фронт может уменьшать, если машина слабая
    //Если террейнов нет, в казанном квадрате, то генерируются на 1 шире для более плавной подгрузки
    protected Optional<Integer> maxTerrainViewDistance;

    //Если текущая высота камеры над террейном превышает эту высоты, то переключаемся на это разрешение
    public int getRelativeHeightFrom() {
        //базовая реализация c вычислением на основе latStep , чтобы угол обзора был приемлемым
        return (int) (0.3 * LATITUDE_ONE_DEGREE_IN_METERS * latStep);
    }
}
