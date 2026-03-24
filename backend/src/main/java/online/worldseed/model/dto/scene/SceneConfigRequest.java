package online.worldseed.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import online.worldseed.model.dto.scene.core.GeodeticPosition;

import java.util.Optional;

@Data
@Schema(description = "Запрос стартовой конфигурации")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneConfigRequest {
    @Schema(description = "Геодезическая позиция (не передается в первый заход)")
    Optional<GeodeticPosition> geodeticPosition;
}
