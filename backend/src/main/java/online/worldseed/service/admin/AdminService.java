package online.worldseed.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.repository.TerrainRepository;
import online.worldseed.service.MinioService;
import online.worldseed.service.scene.PlanetService;
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
    private final AtomicBoolean dropInProgress = new AtomicBoolean(false);

    /**
     * Удаление всех террейнов
     */
    @Async
    public void dropAllTerrains() {
        if (!dropInProgress.compareAndSet(false, true)) {
            log.info("Drop all terrains is already in progress");
            return;
        }
        try {
            log.info("Start to drop all terrains");
            minioService.clearTerrainBucket();
            terrainRepository.deleteAll();
            //Перегенерируем планетойды
            planetService.regenerate();
            log.info("All terrains dropped successfully");
        } finally {
            dropInProgress.set(false);
        }
    }

    public Boolean getDropInProgress() {
        return dropInProgress.get();
    }
}
