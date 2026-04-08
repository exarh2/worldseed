package online.worldseed.model.generator;

import online.worldseed.model.generator.resolution.Resolution;
import org.locationtech.jts.geom.Envelope;

import java.util.Optional;

/**
 * Запрос на генерацию единичного террейна
 */

public record TerrainGenerationRequest(
        Resolution resolution,
        Envelope terrainEnvelop,
        Optional<Boolean> doubling
) {
}
