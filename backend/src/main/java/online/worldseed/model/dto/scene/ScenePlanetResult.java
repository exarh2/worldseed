package online.worldseed.generator.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Результат состояния сцены с планетойдом")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScenePlanetResult {
    @NotNull
    @Schema(description = "Пути террейна в хранилище")
    private String terrainPath;
}
