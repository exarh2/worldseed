package online.worldseed.generator.service.generator.model;

import online.worldseed.generator.service.generator.model.option.Resolution;
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
