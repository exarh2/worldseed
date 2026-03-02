package online.worldseed.generator.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
}
