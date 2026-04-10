package online.worldseed.model.generator.resolution;

import lombok.Getter;
import online.worldseed.config.properties.GeneratorProperties;

import java.util.Map;
import java.util.Objects;

@Getter
public enum TerrainCompression {
    OFF,
    MESHOPT,
    DRACO,
    DRACO_LAZY;

    public GeneratorProperties.CompressionProperties compressionProperties;

    public static void applyConfiguration(
        Map<TerrainCompression, GeneratorProperties.CompressionProperties> compressionPropertiesByType
    ) {
        Objects.requireNonNull(compressionPropertiesByType, "generator.compression must be configured");

        for (var compressionType : values()) {
            compressionType.compressionProperties = Objects.requireNonNull(
                compressionPropertiesByType.get(compressionType),
                "Missing generator.compression." + compressionType.name() + " configuration"
            );
        }
    }

    public boolean compressOnGenerate() {
        return this != OFF && compressionProperties.getEnabled() && !compressionProperties.getLazy();
    }

    public boolean compressInBackground() {
        return this != OFF && compressionProperties.getEnabled() && compressionProperties.getLazy();
    }
}
