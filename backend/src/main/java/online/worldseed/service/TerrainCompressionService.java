package online.worldseed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.exception.ServiceErrorException;
import online.worldseed.model.properties.GeneratorProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainCompressionService {
    private static final String INPUT_TOKEN = "{input}";
    private static final String OUTPUT_TOKEN = "{output}";

    private final GeneratorProperties generatorProperties;

    /**
     * Оптимизирует GLB через внешний CLI (например gltf-transform/gltfpack).
     * При выключенной оптимизации возвращает исходный байтовый массив.
     */
    public byte[] compress(byte[] sourceGlb, String sourcePathTag) {
        if (!generatorProperties.getTerrainCompression().isEnabled()) {
            return sourceGlb;
        }

        var commandTemplate = generatorProperties.getTerrainCompression().getCommand();
        if (commandTemplate == null || commandTemplate.isEmpty()) {
            log.warn("Terrain compression enabled, but command is empty. Source path: {}", sourcePathTag);
            return sourceGlb;
        }

        if (!containsRequiredTokens(commandTemplate)) {
            var message = "Terrain compression command must contain {input} and {output} tokens";
            if (generatorProperties.getTerrainCompression().getFailOnError()) {
                throw new ServiceErrorException(message);
            }
            log.warn("{}; fallback to original GLB. Source path: {}", message, sourcePathTag);
            return sourceGlb;
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
            var completed = process.waitFor(generatorProperties.getTerrainCompression().getTimeoutMs(), TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                return handleFailure("Terrain compression process timed out", processOutput, sourcePathTag, sourceGlb);
            }

            if (process.exitValue() != 0) {
                return handleFailure("Terrain compression process failed", processOutput, sourcePathTag, sourceGlb);
            }

            var compressedGlb = Files.readAllBytes(outputFile);
            if (compressedGlb.length == 0) {
                return handleFailure("Terrain compression produced empty file", processOutput, sourcePathTag, sourceGlb);
            }
            return compressedGlb;
        } catch (Exception e) {
            if (generatorProperties.getTerrainCompression().getFailOnError()) {
                throw new ServiceErrorException("Terrain compression failed for " + sourcePathTag, e);
            }
            log.warn("Terrain compression failed for {}. Fallback to original GLB", sourcePathTag, e);
            return sourceGlb;
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

    private byte[] handleFailure(String reason, String processOutput, String sourcePathTag, byte[] sourceGlb) {
        var message = reason + ". Source path: " + sourcePathTag + ". Output: " + processOutput;
        if (generatorProperties.getTerrainCompression().getFailOnError()) {
            throw new ServiceErrorException(message);
        }
        log.warn("{}; fallback to original GLB", message);
        return sourceGlb;
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
