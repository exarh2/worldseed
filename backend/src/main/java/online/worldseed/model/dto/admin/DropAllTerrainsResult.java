package online.worldseed.generator.model.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Результат удаления всех террейнов")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DropAllTerrainsResult {
    @NotNull
    @Schema(description = "Идет удаление террейнов")
    private Boolean inProgress;
}
