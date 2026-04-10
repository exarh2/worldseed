package online.worldseed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.worldseed.model.dto.scene.SceneConfigResult;
import online.worldseed.model.dto.scene.SceneGenerationStateRequest;
import online.worldseed.model.dto.scene.ScenePlanetResult;
import online.worldseed.model.dto.scene.SceneStateRequest;
import online.worldseed.model.dto.scene.SceneStateResult;
import online.worldseed.model.dto.scene.core.GeocentricPosition;
import online.worldseed.model.dto.scene.core.GeodeticPosition;
import online.worldseed.model.generator.resolution.Resolution;
import online.worldseed.service.scene.PlanetService;
import online.worldseed.service.scene.SceneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static online.worldseed.model.dto.exception.StatusDescription.HTTP_400_DESC;
import static online.worldseed.model.dto.exception.StatusDescription.HTTP_501_DESC;

@Tag(name = "API загрузки террейнов на сцену")
@RestController
@RequestMapping(path = "/api/v1/scene")
@RequiredArgsConstructor
public class SceneController {
    private final SceneService sceneService;
    private final PlanetService planetService;

    @Operation(summary = "Стартовая конфигурация",
        responses = {
            @ApiResponse(responseCode = "200", description = "Стартовая конфигурация"),
            @ApiResponse(responseCode = "400", description = HTTP_400_DESC)
        })
    @GetMapping("/config")
    public SceneConfigResult getSceneConfig() {
        return sceneService.getSceneConfig();
    }

    @Operation(summary = "Высота и геоцентрическая позиция по геодезическим координатам",
        responses = {
            @ApiResponse(responseCode = "200", description = "Геоцентрическая позиция с высотой"),
            @ApiResponse(responseCode = "400", description = HTTP_400_DESC)
        })
    @PostMapping("/alt-by-position")
    public GeocentricPosition getAltByPosition(@Valid @RequestBody GeodeticPosition geodeticPosition) {
        return sceneService.getAltByPosition(geodeticPosition);
    }

    @Operation(summary = "Получение ссылки на планетойд для переданного разрешения",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список готовых и генерируемых террейнов"),
            @ApiResponse(responseCode = "400", description = HTTP_400_DESC),
            @ApiResponse(responseCode = "501", description = HTTP_501_DESC)
        })
    @GetMapping("/planet/{resolution}")
    public ScenePlanetResult getScenePlanet(@Parameter(description = "Разрешение", required = true, example = "R_3")
                                            @PathVariable(name = "resolution") Resolution resolution) {
        return planetService.getScenePlanet(resolution);
    }

    @Operation(summary = "Состояние сцены для заданных координат",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список готовых и генерируемых террейнов"),
            @ApiResponse(responseCode = "400", description = HTTP_400_DESC),
            @ApiResponse(responseCode = "501", description = HTTP_501_DESC)
        })
    @PostMapping
    public SceneStateResult getSceneState(@Valid @RequestBody SceneStateRequest sceneStateRequest) {
        return sceneService.getSceneState(sceneStateRequest);
    }

    @Operation(summary = "Запрос состояния генерации террейнов",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список готовых и генерируемых террейнов"),
            @ApiResponse(responseCode = "400", description = HTTP_400_DESC)
        })
    @PostMapping("/generation")
    public SceneStateResult getSceneGenerationState(@Valid @RequestBody SceneGenerationStateRequest sceneGenerationStateRequest) {
        return sceneService.getSceneGenerationState(sceneGenerationStateRequest);
    }
}
