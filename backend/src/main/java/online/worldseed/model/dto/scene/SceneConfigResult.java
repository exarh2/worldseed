package online.worldseed.generator.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import online.worldseed.generator.model.dto.scene.core.GeocentricPosition;
import online.worldseed.generator.model.dto.scene.core.SceneTerrainOptions;

import java.util.List;
import java.util.Optional;

@Data
@Builder
@Schema(description = "Настройки приложения")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneConfigResult {
    @Schema(description = "Геоцентрическая позиция с высотой (возвращается, если передана геодезическая)")
    private Optional<GeocentricPosition> geocentricPosition;
    @Schema(description = "Настройки террейнов в зависимости от разрешения")
    @NotEmpty
    private List<SceneTerrainOptions> sceneTerrainOptions;
}
