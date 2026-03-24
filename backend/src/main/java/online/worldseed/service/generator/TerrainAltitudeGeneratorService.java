package online.worldseed.service.generator;

import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.service.generator.gltf.AltitudeGltfModelCreator;
import online.worldseed.service.generator.model.Geocentric;
import online.worldseed.service.generator.model.GeocentricTriangle;
import online.worldseed.service.generator.model.option.AltitudeTerrainOptions;
import online.worldseed.service.generator.model.option.Resolution;
import online.worldseed.service.generator.utils.TerrainMath;
import online.worldseed.service.generator.utils.TerrainSlicing;
import online.worldseed.service.generator.utils.Triangulation;
import online.worldseed.service.srtm.DigitalElevationModelProvider;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealMatrix;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static online.worldseed.service.generator.model.GeoConstants.GD_GEOMETRY_FACTORY;
import static online.worldseed.service.generator.model.TerrainGenerationType.TERRAIN_ALTITUDE;

/**
 * Генерация террейнов с минимальными высотными данными (для построения рельефа ЧАСТИ земной поверхности) TERRAIN_ALTITUDE
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainAltitudeGeneratorService {
    private final AltitudeGltfModelCreator gltfModelCreator;
    private final DigitalElevationModelProvider demProvider;

    /**
     * Генерация высотных террейнов в заданном поисковом полигоне
     */
    @SneakyThrows
    public DefaultGltfModel generateTerrains(Resolution resolution, Envelope searchEnvelope) {
        if (resolution.getTerrainOptions().getGenerationType() != TERRAIN_ALTITUDE) {
            throw new UnsupportedOperationException();
        }
        var terrainOptions = (AltitudeTerrainOptions) resolution.getTerrainOptions();
        var nodeModels = TerrainSlicing.coveringTerrainEnvelops(resolution, searchEnvelope).stream()
                .map(p -> generateTerrainNodeModel(terrainOptions, p.getFirst(), p.getSecond())).toList();
        return gltfModelCreator.createGltfFromNodeList(nodeModels);
    }

    /**
     * Генерация одиночного террейна
     */
    @SneakyThrows
    public DefaultGltfModel generateTerrain(Resolution resolution, Envelope terrainEnvelop, Boolean doubling) {
        if (resolution.getTerrainOptions().getGenerationType() != TERRAIN_ALTITUDE) {
            throw new UnsupportedOperationException();
        }
        var terrainOptions = (AltitudeTerrainOptions) resolution.getTerrainOptions();
        return gltfModelCreator.createGltfFromNode(generateTerrainNodeModel(terrainOptions, terrainEnvelop, doubling));
    }

    /**
     * Генерация ноды и модели террейна с высотными данными в виде сетки
     */
    private DefaultNodeModel generateTerrainNodeModel(AltitudeTerrainOptions terrainOptions, Envelope terrainEnvelop, Boolean doubling) {
        Integer gridSize = terrainOptions.getGridSize();
        //Матрицы прямого и обратного преобразования для этого террейна
        var terrainMatrices = TerrainMath.calculateTerrainMatrices(terrainEnvelop);
        //Построение объемлющей сетки из полигонов размера resolution.gridSize + 2 вместе с высотными данными
        var outboundGridPolygons = getOutboundGridPolygons(terrainEnvelop, gridSize, doubling);
        //Триангулируем полигоны грида в геодезических координатах
        var gdTriangles = outboundGridPolygons.stream()
                .map(p -> Triangulation.refinedTriangulation(p).stream()
                        .peek(t -> t.setUserData(p.getUserData())).toList())
                .flatMap(Collection::stream).toList();
        //Если хотим посмотреть разбиение на треугольники на плоскости
        //if (doubling) convertToPlaneImage(gdTriangles, "plane.jpg");
        //Переводим треугольники в геоцентрические координаты
        //Для оптимизации - те же самые точки грида (условно) в других треугольниках не высчитываем, а подменяем ссылку
        var gdGcMap = new HashMap<Coordinate, Geocentric>();
        var gcTriangles = gdTriangles.stream().map(
                gdTriangle -> new GeocentricTriangle(Arrays.stream(gdTriangle.getCoordinates()).limit(3)
                        .map(gdCoordinate -> {
                            var approx = new Coordinate(TerrainMath.roundAvoid(gdCoordinate.getX()),
                                    TerrainMath.roundAvoid(gdCoordinate.getY()));
                            return gdGcMap.computeIfAbsent(approx,
                                    key -> this.convertToGeocentric(gdCoordinate, terrainMatrices.directMatrix()));
                        })
                        .toList(), (Boolean) gdTriangle.getUserData())
        ).toList();
        //Установка нормалей для всех уникальных координат треугольников
        setNormals(gcTriangles);
        //Удаляем граничные треугольники, которые нужны были только для вычисления нормалей на гранях
        gcTriangles = gcTriangles.stream().filter(t -> !t.isTemporal()).toList();
        return gltfModelCreator.createNodeModel(gcTriangles, Optional.empty(), terrainMatrices);
    }

    /**
     * Построение объемлющей сетки из полигонов размера resolution.gridSize вместе с высотными данными
     * Сетка с увеличенным размером нужна для построения правильных нормалей на границах террейна
     */
    private List<Polygon> getOutboundGridPolygons(Envelope envelop, Integer gridSize, Boolean doubling) {
        var polygons = new ArrayList<Polygon>();
        var dem = demProvider.loadDemForArea(demProvider.getDemOutboundEnvelop(envelop, gridSize));
        var latStep = (envelop.getMaxY() - envelop.getMinY()) / gridSize;
        var lonStep = (envelop.getMaxX() - envelop.getMinX()) / gridSize;
        for (int h = -1; h <= gridSize; h++) {
            var lat = envelop.getMinY() + latStep * h;
            if (lat + latStep > 90 || lat < -90) {
                continue;
            }
            for (int w = -1; w <= gridSize; w++) {
                var lon = envelop.getMinX() + lonStep * w;
                if (lon < -180 || lon + lonStep > 180) {
                    continue;
                }
                var gdCoordinates = new Coordinate[]{
                        new Coordinate(lon, lat, dem.getAlt(lon, lat)),
                        new Coordinate(lon + lonStep, lat, dem.getAlt(lon + lonStep, lat)),
                        new Coordinate(lon + lonStep, lat + latStep, dem.getAlt(lon + lonStep, lat + latStep)),
                        new Coordinate(lon, lat + latStep, dem.getAlt(lon, lat + latStep)),
                        new Coordinate(lon, lat, dem.getAlt(lon, lat)),
                };
                var north = envelop.getMinY() >= 0;
                if (doubling && (north && h <= 0 || !north && h >= gridSize - 1)) {
                    var coordinates = new ArrayList<>(Arrays.asList(gdCoordinates));
                    if (north && h == 0 || !north && h == gridSize) {
                        //Добавление посредине нижней грани полигона
                        var c0 = coordinates.get(0);
                        var c1 = coordinates.get(1);
                        coordinates.add(1, new Coordinate((c0.getX() + c1.getX()) / 2, c0.getY(),
                                dem.getAlt((c0.getX() + c1.getX()) / 2, c0.getY())));
                    } else {
                        //Добавление посредине верхней грани полигона
                        var c2 = coordinates.get(2);
                        var c3 = coordinates.get(3);
                        coordinates.add(3, new Coordinate((c2.getX() + c3.getX()) / 2, c2.getY(),
                                dem.getAlt((c2.getX() + c3.getX()) / 2, c2.getY())));
                    }
                    gdCoordinates = coordinates.toArray(Coordinate[]::new);
                }
                var polygon = GD_GEOMETRY_FACTORY.createPolygon(gdCoordinates);
                //Временный треугольник, нужный только для вычисления нормалей на стыках
                var temporal = h == -1 || h == gridSize || w == -1 || w == gridSize;
                polygon.setUserData(temporal);
                polygons.add(polygon);
            }
        }
        return polygons;
    }

    /**
     * Преобразование геодезической координаты в геоцентрическую и прямой перенос в центр геоцентрических координат
     */
    private Geocentric convertToGeocentric(Coordinate gdCoordinate, RealMatrix directMatrix) {
        var rv = directMatrix.operate(Geocentric.fromGeodeticCoordinate(gdCoordinate).toRealVector());
        return new Geocentric(rv.getEntry(0), rv.getEntry(1), rv.getEntry(2), Optional.empty(), Optional.empty());
    }

    /**
     * Установка нормалей для всех уникальных координат треугольников
     */
    private void setNormals(List<GeocentricTriangle> triangles) {
        triangles.stream()
                .flatMap(gcTriangle -> gcTriangle.getGcCoordinates().stream().map(gc -> Pair.of(gc, gcTriangle)))
                .collect(Collectors.groupingBy(Pair::getFirst, Collectors.mapping(Pair::getSecond, Collectors.toList())))
                .forEach((geocentric, gcTriangles) ->
                        //По координатам всех треугольников нет смысла устанавливать нормаль - instance координаты один и тот же
                        geocentric.setNormal(Optional.of(gcTriangles.stream().map(TerrainMath::planeNormal)
                                .reduce(Vector3D::add).orElse(Vector3D.ZERO).normalize())));
    }
}
