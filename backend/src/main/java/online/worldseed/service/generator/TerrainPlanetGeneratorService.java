package online.worldseed.generator.service.generator;

import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.generator.service.generator.gltf.PlanetGltfModelCreator;
import online.worldseed.generator.service.generator.model.Geocentric;
import online.worldseed.generator.service.generator.model.GeocentricTriangle;
import online.worldseed.generator.service.generator.model.option.PlanetTerrainOptions;
import online.worldseed.generator.service.generator.model.option.Resolution;
import online.worldseed.generator.service.generator.utils.TerrainMath;
import online.worldseed.generator.service.generator.utils.TerrainSlicing;
import online.worldseed.generator.service.generator.utils.Triangulation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealMatrix;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static online.worldseed.generator.service.generator.model.GeoConstants.GD_GEOMETRY_FACTORY;
import static online.worldseed.generator.service.generator.model.TerrainGenerationType.TERRAIN_PLANET;

/**
 * Генерация глобуса на основе TERRAIN_PLANET
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainPlanetGeneratorService {
    private final PlanetGltfModelCreator planetGltfModelCreator;

    /**
     * Генерация глобуса земли для разрешенных разрешений
     */
    public DefaultGltfModel generateEarthPlanet(Resolution resolution) {
        if (resolution.getTerrainOptions().getGenerationType() != TERRAIN_PLANET) {
            throw new UnsupportedOperationException();
        }
        var terrainOptions = (PlanetTerrainOptions) resolution.getTerrainOptions();
        var searchEnvelope = new Envelope(new Coordinate(-180, -90), new Coordinate(180, 90));
        var nodeModels = TerrainSlicing.coveringTerrainEnvelops(resolution, searchEnvelope)
                .stream().map(p -> generatePlainTerrainNodeModel(terrainOptions, p.getFirst(), p.getSecond())).toList();
        return planetGltfModelCreator.createGltfFromNodeList(nodeModels);
    }

    /**
     * Генерация ноды и модели террейна-подложки на 0-й высоте
     */
    private DefaultNodeModel generatePlainTerrainNodeModel(PlanetTerrainOptions terrainOptions, Envelope terrainEnvelop, Boolean doubling) {
        //Матрицы прямого и обратного преобразования для этого террейна
        var terrainMatrices = TerrainMath.calculateTerrainMatrices(terrainEnvelop);
        //Построение полигона по конверту
        var terrainPolygon = getTerrainPolygon(terrainEnvelop, doubling);
        //Триангулируем в геодезических координатах
        var gdTriangles = Triangulation.refinedTriangulation(terrainPolygon);
        //convertToPlaneImage(gdTriangles, "plane.jpg");
        //Переводим треугольники в геоцентрические координаты
        //Для оптимизации - те же самые точки в других треугольниках не высчитываем, а подменяем ссылку
        var gdGcMap = new HashMap<Coordinate, Geocentric>();
        var gcTriangles = gdTriangles.stream().map(
                gdTriangle -> new GeocentricTriangle(Arrays.stream(gdTriangle.getCoordinates()).limit(3)
                        .map(gdCoordinate -> gdGcMap.computeIfAbsent(gdCoordinate,
                                key -> this.convertToGeocentricWithNormalAndUV(key, terrainMatrices.directMatrix())))
                        .toList(), false)
        ).toList();
        return planetGltfModelCreator.createNodeModel(gcTriangles, Optional.of(terrainOptions.getTextureSource()), terrainMatrices);
    }

    /**
     * Построение полигона по конверту
     * Если было удвоение - то добавляется одна точка
     */
    private Polygon getTerrainPolygon(Envelope terrainEnvelop, Boolean doubling) {
        var terrainPolygon = JTS.toPolygon(JTS.toRectangle2D(terrainEnvelop));
        if (doubling) {
            var coordinates = new ArrayList<>(Arrays.asList(terrainPolygon.getCoordinates()));
            if (coordinates.get(0).getY() >= 0) {
                var c0 = coordinates.get(0);
                var c1 = coordinates.get(1);
                coordinates.add(1, new Coordinate((c0.getX() + c1.getX()) / 2, c0.getY()));
            } else {
                var c2 = coordinates.get(2);
                var c3 = coordinates.get(3);
                coordinates.add(3, new Coordinate((c2.getX() + c3.getX()) / 2, c2.getY()));
            }
            terrainPolygon = GD_GEOMETRY_FACTORY.createPolygon(coordinates.toArray(Coordinate[]::new));
        }
        return terrainPolygon;
    }

    /**
     * Преобразование геодезической координаты в геоцентрическую с рассчетом нормали и UV
     * и прямой перенос в центр геоцентрических координат
     */
    private Geocentric convertToGeocentricWithNormalAndUV(Coordinate gdCoordinate, RealMatrix directMatrix) {
        var gc = Geocentric.fromGeodeticCoordinate(gdCoordinate);
        var rv = directMatrix.operate(gc.toRealVector());

        //Поднимем точку на километр
        var gdCoordinateAlt = (Coordinate) gdCoordinate.clone();
        gdCoordinateAlt.setZ(1000);
        var gcAlt = Geocentric.fromGeodeticCoordinate(gdCoordinateAlt);
        var normal = Optional.of(new Vector3D(gcAlt.getX() - gc.getX(), gcAlt.getY() - gc.getY(),
                gcAlt.getZ() - gc.getZ()).normalize());

        //https://ru.wikipedia.org/wiki/UV-преобразование
        var u = (float) (toRadians(gdCoordinate.getX()) / (2 * PI) + 0.5);
        var v = 1 - (float) (toRadians(gdCoordinate.getY()) / PI + 0.5);
        var uv = Optional.of(Pair.of(u, v));
        return new Geocentric(rv.getEntry(0), rv.getEntry(1), rv.getEntry(2), normal, uv);
    }
}
