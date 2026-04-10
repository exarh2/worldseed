package online.worldseed.service.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.generator.resolution.Resolution;
import online.worldseed.repository.TerrainRepository;
import online.worldseed.service.MinioService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainLazyCompressionService {
    private final TerrainRepository terrainRepository;
    private final TerrainCompressionService terrainCompressionService;
    private final MinioService minioService;

    @Scheduled(fixedDelayString = "${services.compression.lazy-run-delay-ms:30000}")
    public void compressPendingLazyTerrains() {
        var lazyResolutions = Arrays.stream(Resolution.values())
            .filter(resolution -> resolution.getTerrainOptions().getCompression().compressInBackground())
            .collect(Collectors.toList());
        if (lazyResolutions.isEmpty()) {
            return;
        }

        var page = PageRequest.of(0, 10);
        var notCompressedTerrains = terrainRepository.findAllByCompressedFalseAndResolutionIn(lazyResolutions, page);
        for (var terrain : notCompressedTerrains) {
            try {
                var compressionType = terrain.getResolution().getTerrainOptions().getCompression();
                var terrainBinary = minioService.getTerrainBinary(terrain.getStoragePath());
                var optimizedBinary = terrainCompressionService.compress(
                    terrainBinary,
                    compressionType
                );
                minioService.overwriteTerrainBinary(terrain.getStoragePath(), optimizedBinary);
                terrain.setCompressed(true);
                terrainRepository.save(terrain);
            } catch (Exception e) {
                log.error("Lazy terrain compression failed for terrainId={}, storagePath={}",
                    terrain.getId(), terrain.getStoragePath(), e);
            }
        }
    }
}
