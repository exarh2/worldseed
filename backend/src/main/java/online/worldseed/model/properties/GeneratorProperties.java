package online.worldseed.model.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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

    @Positive
    @Builder.Default
    private int overpassConnectTimeoutMs = 5000;

    @Positive
    @Builder.Default
    private int overpassReadTimeoutMs = 10000;
}
