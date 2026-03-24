package online.worldseed.generator.service.scene;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.generator.mapper.DefaultMapper;
import online.worldseed.generator.model.dto.scene.SceneConfigRequest;
import online.worldseed.generator.model.dto.scene.SceneConfigResult;
import online.worldseed.generator.model.dto.scene.SceneGenerationStateRequest;
import online.worldseed.generator.model.dto.scene.SceneStateRequest;
import online.worldseed.generator.model.dto.scene.SceneStateResult;
import online.worldseed.generator.model.dto.scene.core.GeocentricPosition;
import online.worldseed.generator.model.entity.TerrainEntity;
import online.worldseed.generator.repository.TerrainRepository;
import online.worldseed.generator.service.generator.TerrainGeneratorService;
import online.worldseed.generator.service.generator.model.Geocentric;
import online.worldseed.generator.service.generator.model.Geodetic;
import online.worldseed.generator.service.generator.model.TerrainGenerationRequest;
import online.worldseed.generator.service.generator.model.option.AltitudeTerrainOptions;
import online.worldseed.generator.service.generator.model.option.Resolution;
import online.worldseed.generator.service.generator.utils.TerrainSlicing;
import online.worldseed.generator.service.srtm.DigitalElevationModelProvider;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static online.worldseed.generator.service.generator.model.TerrainGenerationType.TERRAIN_PLANET;

/**
 * Загрузки террейнов на сцену
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {
    private final TerrainRepository terrainRepository;
    private final TerrainGeneratorService terrainGeneratorService;
    private final DigitalElevationModelProvider digitalElevationModelProvider;
    private final DefaultMapper mapper;

    /**
     * Стартовая конфигурация
     */
    public SceneConfigResult getSceneConfig(@Valid SceneConfigRequest sceneConfigRequest) {
        var geocentricPosition = Optional.<GeocentricPosition>empty();
        if (sceneConfigRequest.getGeodeticPosition().isPresent()) {
            var pos = sceneConfigRequest.getGeodeticPosition().get();
            //Пока минимальное разрешение считаем R_1_64
            var altitudeTerrainOptions = (AltitudeTerrainOptions) Resolution.R_1_64.getTerrainOptions();
            var minEnvelop = TerrainSlicing.getSearchEnvelop(pos.getLon(), pos.getLat(),
                    altitudeTerrainOptions.getLatStep() / altitudeTerrainOptions.getGridSize());
            var dem = digitalElevationModelProvider.loadDemForArea(minEnvelop);
            var alt = dem.getAlt(pos.getLon(), pos.getLat());
            var geocentric = Geocentric.fromGeodetic(new Geodetic(pos.getLat(), pos.getLon(), alt));
            geocentricPosition = Optional.of(new GeocentricPosition(geocentric.getX(), geocentric.getY(), geocentric.getZ(), alt));
        }
        return SceneConfigResult.builder()
                .geocentricPosition(geocentricPosition)
                .sceneTerrainOptions(Arrays.stream(Resolution.values()).map(mapper::toSceneTerrainOptions).toList())
                .build();
    }

    /**
     * Получение состояния сцены
     */
    public SceneStateResult getSceneState(SceneStateRequest sceneStateRequest) {
        var terrainOptions = sceneStateRequest.getResolution().getTerrainOptions();
        if (terrainOptions.getGenerationType() == TERRAIN_PLANET) {
            throw new UnsupportedOperationException();
        }
        var terrainViewDistance = sceneStateRequest.getTerrainViewDistance() > terrainOptions.getMaxTerrainViewDistance().get() ?
                terrainOptions.getMaxTerrainViewDistance().get() : sceneStateRequest.getTerrainViewDistance();
        var viewDistance = sceneStateRequest.getResolution().getTerrainOptions().getLatStep() * terrainViewDistance;
        var searchEnvelop = TerrainSlicing.getSearchEnvelop(
                sceneStateRequest.getLongitude(), sceneStateRequest.getLatitude(), viewDistance);
        //Ищется область на 1 террейн шире для более плавной подгрузки в случае генерации
        var ambientViewDistance = sceneStateRequest.getResolution().getTerrainOptions().getLatStep() * (terrainViewDistance + 1);
        var ambientSearchEnvelop = TerrainSlicing.getSearchEnvelop(
                sceneStateRequest.getLongitude(), sceneStateRequest.getLatitude(), ambientViewDistance);
        //Создадим мапу с ключем - row_key = центр полигона террейна
        var rowKeyTerrainEnvelopWrapperMap = TerrainSlicing.coveringTerrainEnvelops(
                        sceneStateRequest.getResolution(), ambientSearchEnvelop).stream()
                .map(p -> new TerrainEnvelopWrapper(p.getFirst(), p.getSecond(),
                        ambientSearchEnvelop.centre().distance(new Coordinate(p.getFirst().centre().getX(), p.getFirst().centre().getY())),
                        p.getFirst().getMaxX() > searchEnvelop.getMinX() && p.getFirst().getMinX() < searchEnvelop.getMaxX() &&
                                p.getFirst().getMaxY() > searchEnvelop.getMinY() && p.getFirst().getMinY() < searchEnvelop.getMaxY()
                ))
                .collect(Collectors.toMap(tew -> TerrainSlicing.getRowKey(tew.terrainEnvelope()), tew -> tew));
        //В базе для ускорения ищется по хэшу, но на всякий случай дофильтровывается уже по row_key
        var existedRowKeyPairMap = terrainRepository
                .findAllByRowHashIn(rowKeyTerrainEnvelopWrapperMap.keySet().stream().map(String::hashCode).toList())
                .stream()
                .filter(t -> rowKeyTerrainEnvelopWrapperMap.containsKey(t.getRowKey()))
                .collect(Collectors.toMap(TerrainEntity::getRowKey,
                        te -> Pair.of(te, rowKeyTerrainEnvelopWrapperMap.get(te.getRowKey()).distance())));
        //Отправляем на генерацию не существующие террейны отсортированные по расстоянию
        // (чтобы близлежащие генерировались раньше)
        var waitingRowKeys = rowKeyTerrainEnvelopWrapperMap.entrySet().stream()
                .filter(e -> !existedRowKeyPairMap.containsKey(e.getKey()))
                .sorted(Comparator.comparing(entry -> entry.getValue().distance()))
                .peek(entry -> {
                    //Только, что сгенерированные, считаются waiting, но повторно не отправляются
                    if (terrainGeneratorService.checkStoragePath(entry.getKey()) == null) {
                        terrainGeneratorService.generateTerrain(new TerrainGenerationRequest(sceneStateRequest.getResolution(),
                                entry.getValue().terrainEnvelope(), Optional.of(entry.getValue().doubling())));
                    }
                })
                .map(Map.Entry::getKey).toList();
        return SceneStateResult.builder()
                .terrainPaths(existedRowKeyPairMap.values().stream()
                        //Возвращаем только в базовом полигоне
                        .filter(p -> rowKeyTerrainEnvelopWrapperMap.get(p.getFirst().getRowKey()).intersect)
                        .sorted(Comparator.comparing(Pair::getSecond))
                        .map(p -> p.getFirst().getStoragePath()).toList())
                .waitingRowKeys(waitingRowKeys.stream()
                        .filter(wrk -> rowKeyTerrainEnvelopWrapperMap.get(wrk).intersect)
                        .toList())
                .build();
    }

    /**
     * Состояния генерации террейнов
     * На этот метод сильно полагаться не стоит, если возвращает один и тот же список через N попыток дергаем
     * getSceneState
     */
    public SceneStateResult getSceneGenerationState(SceneGenerationStateRequest sceneGenerationStateRequest) {
        var terrainPaths = new ArrayList<String>();
        var waitingRowKeys = new ArrayList<String>();
        sceneGenerationStateRequest.getWaitingRowKeys().forEach(waitingRowKey -> {
            var terrainPath = terrainGeneratorService.checkStoragePath(waitingRowKey);
            if (terrainPath != null) {
                terrainPaths.add(terrainPath);
            } else {
                waitingRowKeys.add(waitingRowKey);
            }
        });
        return SceneStateResult.builder()
                .terrainPaths(terrainPaths)
                .waitingRowKeys(waitingRowKeys)
                .build();
    }

    public record TerrainEnvelopWrapper(Envelope terrainEnvelope, Boolean doubling,
                                        Double distance, Boolean intersect) {
    }
}

