package online.worldseed.model.generator.option;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.worldseed.model.generator.TerrainType;

import java.util.Optional;

/**
 * Кастомные настройки генерации и показа террейнов
 * TODO https://wiki.openstreetmap.org/wiki/Zoom_levels
 */
@Getter
@AllArgsConstructor
public abstract class TerrainOptions {
    //Вид террейна (зависит зависимости от разрешения)
    protected TerrainType generationType;

    //Шаг нарезки сетки по широте
    protected Double latStep;

    //Максимальная видимость вокруг пользователя в террейнах,
    //Фронт может уменьшать, если машина слабая
    //Если террейнов нет, в казанном квадрате, то генерируются на 1 шире для более плавной подгрузки
    protected Optional<Integer> maxTerrainViewDistance;
}
