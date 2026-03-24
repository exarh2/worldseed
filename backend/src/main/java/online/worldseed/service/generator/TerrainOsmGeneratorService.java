//package online.worldseed.generator.service.generator;
//
//import de.javagl.jgltf.model.impl.DefaultGltfModel;
//import de.javagl.jgltf.model.impl.DefaultNodeModel;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import online.worldseed.generator.service.generator.gltf.GltfModelCreator;
//import online.worldseed.generator.service.generator.model.Geocentric;
//import online.worldseed.generator.service.generator.model.option.ResolutionOption;
//import online.worldseed.generator.service.generator.utils.TerrainMath;
//import online.worldseed.generator.service.generator.utils.TerrainSlicing;
//import online.worldseed.generator.service.generator.utils.Triangulation;
//import online.worldseed.generator.service.isohypse.IsohypseGeneratorService;
//import online.worldseed.generator.service.osm.OsmQueryService;
//import online.worldseed.generator.service.srtm.DigitalElevationModelProvider;
//import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
//import org.apache.commons.math3.linear.RealMatrix;
//import org.geotools.geometry.jts.JTS;
//import org.locationtech.jts.geom.Coordinate;
//import org.locationtech.jts.geom.Envelope;
//import org.locationtech.jts.geom.Polygon;
//import org.springframework.data.util.Pair;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.Optional;
//
//import static java.lang.Math.PI;
//import static java.lang.Math.toRadians;
//import static online.worldseed.generator.service.generator.model.GeoConstants.GD_GEOMETRY_FACTORY;
//import static online.worldseed.generator.service.generator.model.TerrainGenerationType.TERRAIN_ALTITUDE;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TerrainOsmGeneratorService {
//    private static final int REFINEMENTS_COUNT = 0;
//
//    private final OsmQueryService osmQueryService;
//    private final IsohypseGeneratorService isohypseGeneratorService;
//    private final GltfModelCreator gltfModelCreator;
//    private final DigitalElevationModelProvider digitalElevationModelProvider;
//
//    /**
//     * Генерация глобуса земли для разрешенных разрешений
//     */
//    public DefaultGltfModel generateEarthPlanet(ResolutionOption resolution) {
//        if (resolution.getGenerationType() != TERRAIN_PLAIN) {
//            throw new UnsupportedOperationException();
//        }
//        var envelope = new Envelope(new Coordinate(-180, -90), new Coordinate(180, 90));
//        var nodeModels = TerrainSlicing.coveringTerrainPolygons(resolution,
//                        JTS.toPolygon(JTS.toRectangle2D(envelope))).values()
//                .stream().map(terrainPolygon -> generateTerrainPlainNode(resolution, terrainPolygon)).toList();
//        return gltfModelCreator.createGltfFromNodeList(nodeModels);
//    }
//
//    /**
//     * Генерация террейнов в заданном полигоне
//     */
//    @SneakyThrows
//    public DefaultGltfModel generateRegionTerrains(ResolutionOption resolution, Envelope envelope) {
//        if (resolution.getGenerationType() != TERRAIN_ALTITUDE) {
//            throw new UnsupportedOperationException();
//        }
//        var nodeModels = TerrainSlicing.coveringTerrainPolygons(resolution,
//                        JTS.toPolygon(JTS.toRectangle2D(envelope))).entrySet()
//                .stream().map(terrainPolygon -> generateTerrainNode(resolution, terrainPolygon.getValue())).toList();
//        return gltfModelCreator.createGltfFromNodeList(nodeModels);
//    }
//
//
//    /**
//     * Генерация Ноды и модели террейна с минимальными высотными данными
//     */
//    private DefaultNodeModel generateTerrainNode(ResolutionOption resolution, Polygon terrainPolygon) {
//        //Матрицы прямого и обратного преобразования для этого террейна
//        var terrainMatrices = TerrainMath.calculateTerrainMatrices(terrainPolygon);
//        var envelop = terrainPolygon.getEnvelopeInternal();
//        var dem = digitalElevationModelProvider.loadDemForArea(envelop);
//        var latStep = (envelop.getMaxY() - envelop.getMinY()) / (dem.getHeight() - 1);
//        var lonStep = (envelop.getMaxX() - envelop.getMinX()) / (dem.getWidth() - 1);
//        var polygons = new ArrayList<Polygon>();
//        for (int y = 0; y < dem.getHeight() - 1; y++) {
//            for (int x = 0; x < dem.getWidth() - 1; x++) {
//                var gcCoordinates = new Coordinate[]{
//                        new Coordinate(envelop.getMinX() + x * lonStep, envelop.getMinY() + y * latStep,
//                                dem.getElevationForDataPoint(x, y)),
//                        new Coordinate(envelop.getMinX() + (x + 1) * lonStep, envelop.getMinY() + y * latStep,
//                                dem.getElevationForDataPoint(x + 1, y)),
//                        new Coordinate(envelop.getMinX() + (x + 1) * lonStep, envelop.getMinY() + (y + 1) * latStep,
//                                dem.getElevationForDataPoint(x + 1, y + 1)),
//                        new Coordinate(envelop.getMinX() + x * lonStep, envelop.getMinY() + (y + 1) * latStep,
//                                dem.getElevationForDataPoint(x, y + 1)),
//                        new Coordinate(envelop.getMinX() + x * lonStep, envelop.getMinY() + y * latStep,
//                                dem.getElevationForDataPoint(x, y))
//                };
//                polygons.add(GD_GEOMETRY_FACTORY.createPolygon(gcCoordinates));
//            }
//        }
//
//        //Триангулируем в геодезических координатах
//        var gdTriangles = polygons.stream().map(p ->
//                        Triangulation.refinedTriangulation(p, resolution.getRefinements()))
//                .flatMap(Collection::stream).toList();
//        //Если хотим посмотреть разбиение на треугольники на плоскости
//        //convertToPlaneImage(gdTriangles, "plane.jpg");
//        //Переводим треугольники в геоцентрические координаты
//        var gcTriangles = gdTriangles.stream().map(gdTrianglePolygon -> Arrays.stream(gdTrianglePolygon.getCoordinates()).limit(3)
//                .map(gdCoordinate -> convertToGeocentric(gdCoordinate, terrainMatrices.directMatrix()))
//                .toList()).toList();
//        var meshModel = gltfModelCreator.createMeshModel(gcTriangles);
//        return gltfModelCreator.createNodeModel(List.of(meshModel), terrainMatrices.inverseMatrix());
//    }
//
//    /**
//     * Генерация Ноды и модели террейна-подложки на 0-й высоте
//     */
//    private DefaultNodeModel generateTerrainPlainNode(ResolutionOption resolution, Polygon terrainPolygon) {
//        //Матрицы прямого и обратного преобразования для этого террейна
//        var terrainMatrices = TerrainMath.calculateTerrainMatrices(terrainPolygon);
//        //Триангулируем в геодезических координатах
//        var gdTriangles = Triangulation.refinedTriangulation(terrainPolygon, resolution.getRefinements());
//        //Если хотим посмотреть разбиение на треугольники на плоскости
//        //convertToPlaneImage(gdTriangles, "plane.jpg");
//        //Переводим треугольники в геоцентрические координаты
//        var gcTriangles = gdTriangles.stream().map(gdTrianglePolygon -> Arrays.stream(gdTrianglePolygon.getCoordinates()).limit(3)
//                .map(gdCoordinate -> convertToGeocentric(gdCoordinate, terrainMatrices.directMatrix()))
//                .toList()).toList();
//        var meshModel = gltfModelCreator.createMeshModel(gcTriangles);
//        return gltfModelCreator.createNodeModel(List.of(meshModel), terrainMatrices.inverseMatrix());
//    }
//
//    /**
//     * Преобразование геодезической координаты в геоцентрическую с рассчетом нормали и UV
//     */
//    private Geocentric convertToGeocentric(Coordinate gdCoordinate, RealMatrix directMatrix) {
//        var gc = Geocentric.fromGeodeticCoordinate(gdCoordinate);
//        var rv = directMatrix.operate(gc.toRealVector());
//
//        var normal = Optional.<Vector3D>empty();
//        var gdCoordinateAlt = (Coordinate) gdCoordinate.clone();
//        //Поднимем точку на километр
//        gdCoordinateAlt.setZ((Double.isNaN(gdCoordinateAlt.getZ()) ? 0 : gdCoordinateAlt.getZ()) + 1000);
//        var gcAlt = Geocentric.fromGeodeticCoordinate(gdCoordinateAlt);
//        normal = Optional.of(new Vector3D(gcAlt.getX() - gc.getX(), gcAlt.getY() - gc.getY(),
//                gcAlt.getZ() - gc.getZ()).normalize());
//
//        //https://ru.wikipedia.org/wiki/UV-преобразование
//        var u = (float) (toRadians(gdCoordinate.getX()) / (2 * PI) + 0.5);
//        var v = 1 - (float) (toRadians(gdCoordinate.getY()) / PI + 0.5);
//        var uv = Optional.of(Pair.of(u, v));
//        return new Geocentric(rv.getEntry(0), rv.getEntry(1), rv.getEntry(2), normal, uv);
//    }
//
//    /*
//        @SneakyThrows
//        public DefaultNodeModel generateTerrainNode(Polygon terrainPolygon, ResolutionOption resolution) {
//            //Полигон-подложка на 0-й высоте
//            //Матрицы прямого и обратного преобразования для этого террейна
//            var terrainMatrices = TerrainMath.calculateTerrainMatrices(terrainPolygon);
//            var geometries = new ArrayList<Geometry>();
//            if (resolution.getGenerationType() == TERRAIN_PLAIN) {
//                geometries.add(terrainPolygon);
//            } else {
//                //var osmGeometries = new ArrayList(osmQueryService.getGeometriesFromOsm(envelope));
//                //var isohypsesLinesAndPoints = isohypseGeneratorService.generateIsohypses(envelope);
//                //dumpGeoJson( Stream.concat(osmGeometries.stream(), isohypsesLinesAndPoints.stream()).toList(), "terrain.json");
//                //TODO преобразование osmGeometries с учетом изолиний
//                //geometries.addAll(osmGeometries);
//            }
//            //Преобразуем все геометрии геодезического террейна в трехмерные объекты в геоцентрических координатах
//            var meshModels = geometries.stream().map(geometry -> {
//                var metaInfo = (GeometryMetaInfo) geometry.getUserData();
//                if (geometry instanceof Polygon) {
//                    //Триангулируем в геодезических координатах
//                    var gdTriangles = Triangulation.refinedTriangulation(geometry, resolution.getTriangulationRefinements());
//                    //Если хотим посмотреть разбиение на треугольники на плоскости
//                    if (false) {
//                        convertToPlaneImage(gdTriangles, "plane.jpg");
//                    }
//                    //Переводим треугольники в геоцентрические координаты
//                    var gcTriangles = new ArrayList<List<Geocentric>>();
//                    for (int i = 0; i < gdTriangles.getNumGeometries(); i++) {
//                        var gdCoordinates = Arrays.stream(gdTriangles.getGeometryN(i).getCoordinates()).toList().subList(0, 3);
//                        var cnt = gdCoordinates.stream().filter(gd -> abs(gd.getY()) == 90).count();
//                        if (cnt == 2) {
//                            //Пустой треугольник
//                            continue;
//                        }
//                        var gcCoordinates = gdCoordinates.stream()
//                                .map(gdCoordinate -> {
//                                    var gc = Geocentric.fromGeodeticCoordinate(gdCoordinate);
//                                    var rv = terrainMatrices.directMatrix().operate(gc.toRealVector());
//                                    var normal = Optional.<Vector3D>empty();
//                                    var uv = Optional.<Pair<Float, Float>>empty();
//                                    if (resolution.getGenerationType() == TERRAIN_PLAIN) {
//                                        var gdCoordinateAlt = (Coordinate) gdCoordinate.clone();
//                                        //Поднимем точку на километр
//                                        gdCoordinateAlt.setZ(1000);
//                                        var gcAlt = Geocentric.fromGeodeticCoordinate(gdCoordinateAlt);
//                                        normal = Optional.of(new Vector3D(gcAlt.getX() - gc.getX(), gcAlt.getY() - gc.getY(),
//                                                gcAlt.getZ() - gc.getZ()).normalize());
//                                        //https://ru.wikipedia.org/wiki/UV-преобразование
//                                        var u = (float) (toRadians(gdCoordinate.getX()) / (2 * PI) + 0.5);
//                                        var v = 1 - (float) (toRadians(gdCoordinate.getY()) / PI + 0.5);
//                                        uv = Optional.of(Pair.of(u, v));
//                                    }
//                                    return new Geocentric(rv.getEntry(0), rv.getEntry(1), rv.getEntry(2), normal, uv);
//                                })
//                                .toList();
//                        //                    var triangle = GD_GEOMETRY_FACTORY.createPolygon(gcCoordinates);
//                        //                    if (triangle.getArea() != 0) {
//                        //                        gcTriangles.add(triangle);
//                        //                    }
//                        //                    else{
//                        //                        throw new RuntimeException();
//                        //                    }
//
//                        gcTriangles.add(gcCoordinates);
//                    }
//                    return gltfModelCreator.createMeshModel(gcTriangles, metaInfo);
//                } else {
//                    throw new RuntimeException("Unprocessable geometry");
//                }
//            }).toList();
//            return gltfModelCreator.createNodeModel(meshModels, terrainMatrices.inverseMatrix());
//        }
//
//     */
//}
