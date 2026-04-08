package online.worldseed.model.generator.resolution;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * Настройки разбиения сетки в зависимости от разрешения
 * 360 и 90 делятся нацело раз 18-3, 10-3, 9-4, 6-3, 5-4, 4-4, 3-4, 2-3, 1-4, 0.5-5, 0.25-6...
 */
@Getter
public enum Resolution {
    //Начальный размер сетки в районе эквартора 1/128 = ~880m
    R_1_128,
    R_1_64,
    R_1_16,
    R_1_4,
    R_1,
    R_3;
    //R_9(new PlanetTerrainOptions(9.0, "earth1024.jpg"));
    //R_18(new PlanetTerrainOptions(18.0, 1000, "earth1024.jpg"));

    //Кастомные настройки генерации и показа террейнов
    private TerrainOptions terrainOptions;

    public static void applyConfiguration(Map<Resolution, TerrainOptions> terrainOptionsByResolution) {
        Objects.requireNonNull(terrainOptionsByResolution, "generator.resolutions must be configured");

        for (var resolution : values()) {
            resolution.terrainOptions = Objects.requireNonNull(
                    terrainOptionsByResolution.get(resolution),
                    "Missing generator.resolutions." + resolution.name() + " configuration"
            );
        }
    }

}
