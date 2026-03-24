package online.worldseed.generator.model.dto.scene;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос состояния генерации террейнов")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneGenerationStateRequest {
    @NotNull
    @Schema(description = "Ключи террейнов на генерации")
    List<String> waitingRowKeys;
}
