package online.worldseed.generator.model.dto.scene.core;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Геоцентрическая позиция")
public class GeocentricPosition {
    @NotNull
    Double x;
    @NotNull
    Double y;
    @NotNull
    Double z;
    @NotNull
    Double alt;
}
