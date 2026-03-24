package online.worldseed.generator.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import online.worldseed.generator.model.dto.scene.core.GeodeticPosition;

import java.util.Optional;

@Value
@Schema(description = "Запрос стартовой конфигурации")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneConfigRequest {
    @Schema(description = "Геодезическая позиция (не передается в первый заход)")
    Optional<GeodeticPosition> geodeticPosition;
}
