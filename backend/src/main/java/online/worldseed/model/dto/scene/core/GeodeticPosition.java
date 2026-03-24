package online.worldseed.generator.model.dto.scene.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Геодезическая позиция")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GeodeticPosition {
    @NotNull
    @Schema(description = "Долгота (не передается в первый заход)", example = "56.544160")
    double lon;
    @NotNull
    @Schema(description = "Широта (не передается в первый заход)", example = "53.098187")
    double lat;
}
