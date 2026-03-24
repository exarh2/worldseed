package online.worldseed.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import online.worldseed.service.generator.model.option.Resolution;

@Value
@Schema(description = "Запрос состояния сцены (non Planet)")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneStateRequest {
    @NotNull
    @Schema(description = "Разрешение", example = "R_1_64")
    Resolution resolution;
    @NotNull
    @Schema(description = "Долгота", example = "0")
    Double longitude;
    @NotNull
    @Schema(description = "Широта", example = "0")
    Double latitude;
    @NotNull
    @Schema(description = "Видимость вокруг пользователя в террейнах", example = "3")
    Integer terrainViewDistance;
}
