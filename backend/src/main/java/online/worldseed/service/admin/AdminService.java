package online.worldseed.generator.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.generator.repository.TerrainRepository;
import online.worldseed.generator.service.generator.MinioService;
import online.worldseed.generator.service.scene.PlanetService;
import online.worldseed.generator.service.scene.SceneService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Тестовый сервер администрирования
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final TerrainRepository terrainRepository;
    private final MinioService minioService;
    private final PlanetService planetService;
    private AtomicBoolean dropInProgress = new AtomicBoolean(false);

    /**
     * Удаление всех террейнов
     */
    @Async
    public void dropAllTerrains() {
        if (!dropInProgress.get()) {
            log.info("Start to drop all terrains");
            dropInProgress.set(true);
            minioService.clearTerrainBucket();
            terrainRepository.deleteAll();
            //Перегенерируем планетойды
            planetService.regenerate();
            log.info("All terrains dropped successfully");
            dropInProgress.set(false);
        }
    }

    public Boolean getDropInProgress() {
        return dropInProgress.get();
    }
}
