package online.worldseed.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import online.worldseed.model.dto.scene.resolution.TerrainOptionsDto;

import java.util.List;

@Data
@Builder
@Schema(description = "Настройки приложения")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneConfigResult {
    @Schema(description = "Настройки террейнов в зависимости от разрешения")
    @NotEmpty
    private List<TerrainOptionsDto> sceneTerrainOptions;
}
