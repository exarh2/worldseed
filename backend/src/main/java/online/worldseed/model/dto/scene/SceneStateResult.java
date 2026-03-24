package online.worldseed.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Результат состояния сцены")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneStateResult {
    @NotNull
    @Schema(description = "Пути террейнов в хранилище")
    private List<String> terrainPaths;
    @NotNull
    @Schema(description = "Ключи террейнов на генерации")
    private List<String> waitingRowKeys;
}
