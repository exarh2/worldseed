package online.worldseed.service.scene;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.dto.scene.ScenePlanetResult;
import online.worldseed.model.generator.TerrainGenerationRequest;
import online.worldseed.model.generator.option.Resolution;
import online.worldseed.repository.TerrainRepository;
import online.worldseed.service.generator.TerrainGeneratorService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static online.worldseed.model.generator.TerrainGenerationType.TERRAIN_PLANET;

/**
 * Планетойды на сцене
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanetService {
    private final TerrainRepository terrainRepository;
    private final TerrainGeneratorService terrainGeneratorService;
    private final ConcurrentHashMap<Resolution, String> resolutionStorePathMap = new ConcurrentHashMap();

    @PostConstruct
    @SneakyThrows
    private void init() {
        var planetResolutions = Arrays.stream(Resolution.values())
            .filter(resolution -> resolution.getTerrainOptions().getGenerationType() == TERRAIN_PLANET).toList();
        terrainRepository.findAllByResolutionIn(planetResolutions).forEach(terrainEntity -> {
            resolutionStorePathMap.put(terrainEntity.getResolution(), terrainEntity.getStoragePath());
        });
        if (resolutionStorePathMap.isEmpty()) {
            regenerate();
        }
    }

    /**
     * Перегенерация планетойдов
     */
    public void regenerate() {
        log.info("Starting regeneration planets...");
        resolutionStorePathMap.clear();
        Arrays.stream(Resolution.values())
            .filter(resolution -> resolution.getTerrainOptions().getGenerationType() == TERRAIN_PLANET)
            .forEach(resolution -> {
                var terrain = terrainGeneratorService.generateTerrainSync(
                    new TerrainGenerationRequest(resolution,
                        //Чтобы центры не совпадали у row_key чутка смещаю на Latstep
                        new Envelope(new Coordinate(-180 + resolution.getTerrainOptions().getLatStep(), -90),
                            new Coordinate(180, 90)),
                        Optional.empty()
                    )
                );
                resolutionStorePathMap.put(resolution, terrain.getStoragePath());
            });
        log.info("Regeneration planets completed.");
    }

    /**
     * Получение ссылки на планетойд для переданного разрешения
     */
    public ScenePlanetResult getScenePlanet(Resolution resolution) {
        if (resolution.getTerrainOptions().getGenerationType() != TERRAIN_PLANET) {
            throw new UnsupportedOperationException();
        }
        return ScenePlanetResult.builder()
            .terrainPath(resolutionStorePathMap.get(resolution))
            .build();
    }
}

