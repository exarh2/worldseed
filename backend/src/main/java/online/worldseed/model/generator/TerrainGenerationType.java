package online.worldseed.model.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Вид террейна (зависит зависимости от разрешения)
 */
@Getter
@AllArgsConstructor
public enum TerrainGenerationType {
    //Террейн без высотных данных (для построения глобуса)
    //Глобус будет генерироваться один раз и храниться в ФХ (minio)
    TERRAIN_PLANET,
    //Террейн с минимальными высотными данными (для построения рельефа ЧАСТИ земной поверхности)
    TERRAIN_ALTITUDE,
    //Террейн с высотными (изолинии) и OSM-данными
    TERRAIN_OSM;
}
