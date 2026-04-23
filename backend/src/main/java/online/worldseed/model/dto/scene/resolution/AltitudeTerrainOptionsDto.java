package online.worldseed.model.dto.scene.resolution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import online.worldseed.model.generator.resolution.Resolution;

import static online.worldseed.model.generator.TerrainType.TERRAIN_ALTITUDE;

@Getter
@Schema(description = "Настройки высотного террейна")
public class AltitudeTerrainOptionsDto extends TerrainOptionsDto {
    @Schema(description = "Максимальная видимость вокруг пользователя в террейнах")
    private final int maxTerrainViewDistance;

    public AltitudeTerrainOptionsDto(Resolution resolution, double latStep, double zoomFrom, int maxTerrainViewDistance) {
        super(resolution, TERRAIN_ALTITUDE, latStep, zoomFrom);
        this.maxTerrainViewDistance = maxTerrainViewDistance;
    }
}
