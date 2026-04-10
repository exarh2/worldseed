package online.worldseed.service.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.exception.ServiceErrorException;
import online.worldseed.model.generator.resolution.Resolution;
import online.worldseed.model.generator.resolution.TerrainCompression;
import online.worldseed.repository.TerrainRepository;
import online.worldseed.service.MinioService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainCompressionService {
    private static final String INPUT_TOKEN = "{input}";
    private static final String OUTPUT_TOKEN = "{output}";
    private final TerrainRepository terrainRepository;
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
            var startedAt = Instant.now();
            log.debug("Lazy terrain compression started for terrainId={}, storagePath={}",
                terrain.getId(), terrain.getStoragePath());
            try {
                var compressionType = terrain.getResolution().getTerrainOptions().getCompression();
                var terrainBinary = minioService.getTerrainBinary(terrain.getStoragePath());
                var optimizedBinary = compress(terrainBinary, compressionType);
                minioService.overwriteTerrainBinary(terrain.getStoragePath(), optimizedBinary);
                terrain.setCompressed(true);
                terrainRepository.save(terrain);
                var elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();
                log.debug("Lazy terrain compression finished for terrainId={}, storagePath={}, elapsedMs={}",
                    terrain.getId(), terrain.getStoragePath(), elapsedMs);
            } catch (Exception e) {
                var elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();
                log.error("Lazy terrain compression failed for terrainId={}, storagePath={}",
                    terrain.getId(), terrain.getStoragePath(), e);
                log.debug("Lazy terrain compression ended with error for terrainId={}, storagePath={}, elapsedMs={}",
                    terrain.getId(), terrain.getStoragePath(), elapsedMs);
            }
        }
    }

    /**
     * Оптимизирует GLB через внешний CLI (например gltf-transform/gltfpack).
     * При выключенной оптимизации возвращает исходный байтовый массив.
     */
    public byte[] compress(byte[] sourceGlb, TerrainCompression compressionType) {
        if (compressionType == TerrainCompression.OFF) {
            return sourceGlb;
        }

        var compression = compressionType.compressionProperties;
        if (!compression.getEnabled()) {
            return sourceGlb;
        }

        var commandTemplate = compression.getCommand();
        if (commandTemplate == null || commandTemplate.isEmpty()) {
            throw new ServiceErrorException("Terrain compression enabled, but command is empty.");
        }

        if (!containsRequiredTokens(commandTemplate)) {
            throw new ServiceErrorException("Terrain compression command must contain {input} and {output} tokens");
        }

        Path inputFile = null;
        Path outputFile = null;
        try {
            inputFile = Files.createTempFile("terrain-input-", ".glb");
            outputFile = Files.createTempFile("terrain-output-", ".glb");
            Files.write(inputFile, sourceGlb);

            var command = wrapForWindowsIfNeeded(buildCommand(commandTemplate, inputFile, outputFile));
            var processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            var process = processBuilder.start();
            var processOutput = new String(process.getInputStream().readAllBytes());
            var completed = process.waitFor(compression.getTimeoutMs(), TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new ServiceErrorException("Terrain compression process timed out." + ". Output: " + processOutput);
            }

            if (process.exitValue() != 0) {
                throw new ServiceErrorException("Terrain compression process failed." + ". Output: " + processOutput);
            }

            var compressedGlb = Files.readAllBytes(outputFile);
            if (compressedGlb.length == 0) {
                throw new ServiceErrorException("Terrain compression produced empty file." + ". Output: " + processOutput);
            }
            return compressedGlb;
        } catch (Exception e) {
            throw new ServiceErrorException("Terrain compression failed for ", e);
        } finally {
            deleteQuietly(inputFile);
            deleteQuietly(outputFile);
        }
    }

    private boolean containsRequiredTokens(List<String> commandTemplate) {
        var hasInput = commandTemplate.stream().anyMatch(item -> item.contains(INPUT_TOKEN));
        var hasOutput = commandTemplate.stream().anyMatch(item -> item.contains(OUTPUT_TOKEN));
        return hasInput && hasOutput;
    }

    private List<String> buildCommand(List<String> commandTemplate, Path inputFile, Path outputFile) {
        var input = inputFile.toAbsolutePath().toString();
        var output = outputFile.toAbsolutePath().toString();
        var command = new ArrayList<String>(commandTemplate.size());
        for (var item : commandTemplate) {
            command.add(item.replace(INPUT_TOKEN, input).replace(OUTPUT_TOKEN, output));
        }
        return command;
    }

    /**
     * On Windows, {@link ProcessBuilder} uses CreateProcess, which does not resolve {@code .cmd} shims
     * the way {@code cmd.exe} does. npm global installs {@code gltf-transform} as {@code gltf-transform.cmd}.
     */
    private List<String> wrapForWindowsIfNeeded(List<String> command) {
        if (command.isEmpty()) {
            return command;
        }
        var os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            return command;
        }
        var first = command.get(0);
        if ("cmd.exe".equalsIgnoreCase(first) || "cmd".equalsIgnoreCase(first)) {
            return command;
        }
        var wrapped = new ArrayList<String>(command.size() + 2);
        wrapped.add("cmd.exe");
        wrapped.add("/c");
        wrapped.addAll(command);
        return wrapped;
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // no-op
        }
    }
}
