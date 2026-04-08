package online.worldseed.model.dto.scene.resolution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import online.worldseed.model.generator.resolution.Resolution;

import static online.worldseed.model.generator.TerrainType.TERRAIN_OSM;

@Getter
@Schema(description = "Настройки OSM-террейна")
public class OsmTerrainOptionsDto extends TerrainOptionsDto {
    @Schema(description = "Максимальная видимость вокруг пользователя в террейнах")
    private final int maxTerrainViewDistance;

    public OsmTerrainOptionsDto(Resolution resolution, double latStep, double zoomTo, int maxTerrainViewDistance) {
        super(resolution, TERRAIN_OSM, latStep, zoomTo);
        this.maxTerrainViewDistance = maxTerrainViewDistance;
    }
}
