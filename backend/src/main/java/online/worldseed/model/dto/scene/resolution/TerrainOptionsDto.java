package online.worldseed.model.dto.scene.resolution;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import online.worldseed.model.generator.TerrainType;
import online.worldseed.model.generator.resolution.Resolution;

@Getter
@Schema(description = "Базовые настройки террейна")
public abstract class TerrainOptionsDto {
    @NotNull
    @Schema(description = "Настройки разрешения")
    protected Resolution resolution;
    @NotNull
    @Schema(description = "Вид террейна (зависит зависимости от разрешения)")
    protected TerrainType generationType;
    @Schema(description = "Шаг нарезки сетки по широте")
    protected double latStep;
    @Schema(description = "Целевой уровень OSM zoom")
    protected double zoomFrom;

    protected TerrainOptionsDto(Resolution resolution, TerrainType generationType, double latStep, double zoomFrom) {
        this.resolution = resolution;
        this.generationType = generationType;
        this.latStep = latStep;
        this.zoomFrom = zoomFrom;
    }
}
