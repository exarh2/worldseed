package online.worldseed.model.dto.scene.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import online.worldseed.model.generator.TerrainType;
import online.worldseed.model.generator.option.Resolution;

import java.util.Optional;

@Value
@Schema(description = "Настройки показа террейнов")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SceneTerrainOptions {
    @NotNull
    @Schema(description = "Настройки разрешения")
    Resolution resolution;
    @NotNull
    @Schema(description = "Вид террейна (зависит зависимости от разрешения)")
    TerrainType generationType;
    @NotNull
    @Schema(description = "Шаг нарезки сетки по широте")
    Double latStep;
    @Schema(description = "Максимальная видимость вокруг пользователя в террейнах")
    Optional<Integer> maxTerrainViewDistance;
}
