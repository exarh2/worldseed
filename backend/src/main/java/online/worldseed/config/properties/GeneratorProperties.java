package online.worldseed.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import online.worldseed.model.generator.TerrainType;
import online.worldseed.model.generator.resolution.Resolution;
import online.worldseed.model.generator.resolution.TerrainCompression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Конфигурация генератора
 */
@Data
@Builder
@Validated
@ConfigurationProperties("generator")
public class GeneratorProperties {
    /**
     * Сервер Overpass API (при желании можно поднять свой)
     */
    @NotBlank
    private String overpassApiUrl;

    @NotNull
    private int overpassConnectTimeoutMs = 5000;

    @NotNull
    private int overpassReadTimeoutMs;

    @NotNull
    private Map<TerrainCompression, CompressionProperties> compression;

    @NotNull
    private Map<Resolution, ResolutionProperties> resolutions;

    /**
     * Конфигурация внешней оптимизации GLB (draco/meshopt).
     */
    @Data
    @Validated
    public static class CompressionProperties {
        /**
         * Включение оптимизации GLB перед сохранением
         */
        @NotNull
        private boolean enabled;
        /**
         * Команда оптимизации.
         * Поддерживаются шаблоны {input} и {output}.
         */
        @NotNull
        private List<String> command;
        /**
         * Таймаут выполнения команды в ms.
         */
        @NotNull
        private Long timeoutMs;
    }

    @Data
    @Validated
    public static class ResolutionProperties {
        @NotNull
        private TerrainType generationType;

        @NotNull
        private Double latStep;

        @NotNull
        private Double zoomTo;

        private Integer gridSize;

        private Integer maxTerrainViewDistance;

        private String textureSource;

        @NotNull
        private TerrainCompression compression;
    }
}
