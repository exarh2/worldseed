package online.worldseed.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import online.worldseed.config.properties.GeneratorProperties;
import online.worldseed.mapper.ResolutionOptionsMapper;
import online.worldseed.model.generator.option.Resolution;
import online.worldseed.model.generator.option.TerrainOptions;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
@RequiredArgsConstructor
public class ResolutionConfigurationInitializer {
    private final GeneratorProperties generatorProperties;
    private final ResolutionOptionsMapper resolutionOptionsMapper;

    @PostConstruct
    public void initialize() {
        var mappedOptions = new EnumMap<Resolution, TerrainOptions>(Resolution.class);
        generatorProperties.getResolutions().forEach(
                (resolution, properties) -> mappedOptions.put(resolution, resolutionOptionsMapper.toTerrainOptions(properties))
        );
        Resolution.applyConfiguration(mappedOptions);
    }
}
