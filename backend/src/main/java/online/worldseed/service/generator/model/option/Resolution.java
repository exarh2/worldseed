package online.worldseed.service.generator.model.option;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Настройки разбиения сетки в зависимости от разрешения
 * 360 и 90 делятся нацело раз 18-3, 10-3, 9-4, 6-3, 5-4, 4-4, 3-4, 2-3, 1-4, 0.5-5, 0.25-6...
 */
@Getter
@AllArgsConstructor
public enum Resolution {
    //Начальный размер сетки в районе эквартора 1/128 = ~880m
    R_1_128(new OsmTerrainOptions(1 / 128.0, 3)),
    R_1_64(new AltitudeTerrainOptions(1 / 64.0, 3, 18 /*max 18.75*/)),
    R_1_16(new AltitudeTerrainOptions(1 / 16.0, 3, 18 /*max 75*/)),
    R_1_4(new AltitudeTerrainOptions(1 / 4.0, 3, 18 /*max 300*/)),
    R_1(new AltitudeTerrainOptions(1 / 4.0, 3, 18 /*max ?*/)),
    //R_1(new PlanetTerrainOptions(1.0, "earth1024.jpg")),
    //TODO meshopt или draco
    R_3(new PlanetTerrainOptions(3.0, "earth1024.jpg"));
    //R_9(new PlanetTerrainOptions(9.0, "earth1024.jpg"));
    //R_18(new PlanetTerrainOptions(18.0, 1000, "earth1024.jpg"));

    //Кастомные настройки генерации и показа террейнов
    private final TerrainOptions terrainOptions;
}
