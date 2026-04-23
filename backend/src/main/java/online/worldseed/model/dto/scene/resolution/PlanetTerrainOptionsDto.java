package online.worldseed.model.dto.scene.resolution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import online.worldseed.model.generator.resolution.Resolution;

import static online.worldseed.model.generator.TerrainType.TERRAIN_PLANET;

@Getter
@Schema(description = "Настройки планетарного террейна")
public class PlanetTerrainOptionsDto extends TerrainOptionsDto {
    public PlanetTerrainOptionsDto(Resolution resolution, double latStep, double zoomFrom) {
        super(resolution, TERRAIN_PLANET, latStep, zoomFrom);
    }
}
