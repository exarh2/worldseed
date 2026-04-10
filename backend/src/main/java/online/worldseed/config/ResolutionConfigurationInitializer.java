package online.worldseed.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.worldseed.config.properties.GeneratorProperties;
import online.worldseed.mapper.PropertiesMapper;
import online.worldseed.model.generator.resolution.Resolution;
import online.worldseed.model.generator.resolution.TerrainCompression;
import online.worldseed.model.generator.resolution.TerrainOptions;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class ResolutionConfigurationInitializer {
    private final GeneratorProperties generatorProperties;
    private final PropertiesMapper propertiesMapper;

    @PostConstruct
    public void initialize() {
        var mappedOptions = new EnumMap<Resolution, TerrainOptions>(Resolution.class);
        generatorProperties.getResolutions().forEach(
                (resolution, properties) -> mappedOptions.put(resolution, propertiesMapper.toTerrainOptions(properties))
        );
        Resolution.applyConfiguration(mappedOptions);
        TerrainCompression.applyConfiguration(generatorProperties.getCompression());
    }
}
