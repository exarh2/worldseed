package online.worldseed.service.generator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.javagl.jgltf.model.GltfModel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.entity.TerrainEntity;
import online.worldseed.repository.TerrainRepository;
import online.worldseed.service.MinioService;
import online.worldseed.service.generator.model.TerrainGenerationRequest;
import online.worldseed.service.generator.utils.TerrainSlicing;
import org.locationtech.jts.geom.Envelope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static online.worldseed.service.generator.model.TerrainGenerationType.TERRAIN_ALTITUDE;
import static online.worldseed.service.generator.model.TerrainGenerationType.TERRAIN_PLANET;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainGeneratorService {
    //Пока вместо redis храним список на генерацию
    private final Queue<Envelope> generateQueue = new ConcurrentLinkedQueue<>();
    //Atomic guard against duplicate generation requests per rowKey
    private final Set<String> generationInProgress = ConcurrentHashMap.newKeySet();
    //Мапа rowKey - storagePath для последних сгенерированных террейнов
    private final Cache<String, String> generatedCache = CacheBuilder.newBuilder().maximumSize(10000).build();
    private final ConcurrentHashMap<String, String> generatedMap = new ConcurrentHashMap(5000);
    private final TaskExecutor terrainGeneratorTaskExecutor;
    private final TerrainRepository terrainRepository;
    private final TerrainPlanetGeneratorService terrainPlanetGeneratorService;
    private final TerrainAltitudeGeneratorService terrainAltitudeGeneratorService;
    private final MinioService minioService;

    /**
     * Добавление террейна в очередь на генерацию, если он еще не там
     */
    public void generateTerrain(TerrainGenerationRequest terrainGenerationRequest) {
        var rowKey = TerrainSlicing.getRowKey(terrainGenerationRequest.terrainEnvelop());
        if (generationInProgress.add(rowKey)) {
            generateQueue.add(terrainGenerationRequest.terrainEnvelop());
            terrainGeneratorTaskExecutor.execute(new TerrainGenerationTask(terrainGenerationRequest, this));
        }
    }

    /**
     * Террейн только что сгенерировался - есть storagePath
     */
    public String checkStoragePath(String rowKey) {
        return generatedCache.getIfPresent(rowKey);
    }

    /**
     * Синхронная генерация террейна
     */
    public TerrainEntity generateTerrainSync(TerrainGenerationRequest terrainGenerationRequest) {
        var center = terrainGenerationRequest.terrainEnvelop().centre();
        var resolution = terrainGenerationRequest.resolution();
        var terrainId = UUID.randomUUID();
        log.debug("Start generate {}_{} with id = {}", center.getX(), center.getY(), terrainId);
        final GltfModel gltfModel;
        if (resolution.getTerrainOptions().getGenerationType() == TERRAIN_PLANET) {
            gltfModel = terrainPlanetGeneratorService.generateEarthPlanet(resolution);
        } else if (resolution.getTerrainOptions().getGenerationType() == TERRAIN_ALTITUDE) {
            gltfModel = terrainAltitudeGeneratorService.generateTerrain(resolution,
                    terrainGenerationRequest.terrainEnvelop(), terrainGenerationRequest.doubling().get());
        } else {
            throw new UnsupportedOperationException();
        }
        var storagePath = minioService.saveTerrain(terrainId, resolution, center, gltfModel);
        var rowKey = TerrainSlicing.getRowKey(terrainGenerationRequest.terrainEnvelop());
        return terrainRepository.save(TerrainEntity.builder()
                .id(terrainId)
                .resolution(resolution)
                .rowKey(rowKey)
                .storagePath(storagePath)
                .build());
    }

    /**
     * Асинхронная генерация террейна в terrainGeneratorTaskExecutor, запись в minio и БД
     */
    @SneakyThrows
    private void generateTerrainAsync(TerrainGenerationRequest terrainGenerationRequest) {
        var rowKey = TerrainSlicing.getRowKey(terrainGenerationRequest.terrainEnvelop());
        try {
            var terrainEntity = generateTerrainSync(terrainGenerationRequest);
            //Внимание. Удаление из generateQueue происходит раньше, чем завершается транзакция
            // По хорошему надо переделать на ожидание коммита, но сделан обход через generatedCache
            generatedCache.put(terrainEntity.getRowKey(), terrainEntity.getStoragePath());
            log.debug("Finish generate {} with id = {}", terrainEntity.getRowKey(), terrainEntity.getId());
        } finally {
            generateQueue.remove(terrainGenerationRequest.terrainEnvelop());
            generationInProgress.remove(rowKey);
        }
    }

    @RequiredArgsConstructor
    private class TerrainGenerationTask implements Runnable {

        private final TerrainGenerationRequest terrainGenerationRequest;
        private final TerrainGeneratorService terrainGeneratorService;

        @SneakyThrows
        public void run() {
            terrainGeneratorService.generateTerrainAsync(terrainGenerationRequest);
        }
    }
}
