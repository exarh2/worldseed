package online.worldseed.service.generator.utils;

import online.worldseed.model.generator.Geocentric;
import online.worldseed.model.generator.Geodetic;
import online.worldseed.model.generator.option.Resolution;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static online.worldseed.model.generator.GeoConstants.LATITUDE_ONE_DEGREE_IN_METERS;
import static online.worldseed.model.generator.TerrainType.TERRAIN_PLANET;

/**
 * Сервис нарезки земной поверхности в зависимости от разрешения
 */
public final class TerrainSlicing {
    //Соотношение шага долготы к широте, меньше которого шаг долготы удваивается
    private static final Double MIN_LON_LAT_ASPECT_RATIO = 0.7;

    //Связка (разрешение, широта) -> шаг нарезки + флаг удвоения долготы
    private static Map<Resolution, Map<Double, LonStepInfo>> resolutionLatLonStepMap = new HashMap<>();

    static {
        initResolutionLatLonStepMap();
    }

    public static LonStepInfo getLonStepInfo(Resolution resolution, double lat) {
        return resolutionLatLonStepMap.get(resolution).get(abs(lat >= 0 ? lat : lat + resolution.getTerrainOptions().getLatStep()));
    }

    /**
     * Получение сетки террейнов, объемлющих заданный поисковый полигон
     */
    public static List<Pair<Envelope, Boolean>> coveringTerrainEnvelops(Resolution resolution, Envelope searchEnvelope) {
        //Левый нижний угол полигона
        var result = new ArrayList<Pair<Envelope, Boolean>>();
        if (searchEnvelope.getMinX() < -180 || searchEnvelope.getMaxX() > 360 + 180) {
            throw new UnsupportedOperationException();
        }
        var latStep = resolution.getTerrainOptions().getLatStep();
        var latFrom = ((int) floor(searchEnvelope.getMinY() / latStep)) * latStep;
        var latTo = ((int) ceil(searchEnvelope.getMaxY() / latStep)) * latStep;
        for (var lat = latFrom; lat <= latTo - latStep; lat += latStep) {
            var lonStepInfo = getLonStepInfo(resolution, lat);
            var lonStep = lonStepInfo.lonStep();
            var lonFrom = -180 + ((int) floor((searchEnvelope.getMinX() + 180) / lonStep)) * lonStep;
            var lonTo = -180 + ((int) ceil((searchEnvelope.getMaxX() + 180) / lonStep)) * lonStep;
            for (var lon = lonFrom; lon <= lonTo - lonStep; lon += lonStep) {
                //Ищем в районе 180 меридиана
                var realLon = lon >= 180 ? lon - 360 : lon;
                var envelope = new Envelope(new Coordinate(realLon, lat), new Coordinate(realLon + lonStep, lat + latStep));
                result.add(Pair.of(envelope, lonStepInfo.doubling()));
            }
        }
        return result;
    }

    /**
     * Получение обрамляющего поискового полигона
     */
    public static Envelope getSearchEnvelop(double longitude, double latitude, double viewDistance) {
        var longitudeFrom = longitude - viewDistance;
        var longitudeTo = longitude + viewDistance;
        if (longitudeFrom < -180) {
            longitudeFrom += 360;
            longitudeTo += 360;
        }
        var latitudeFrom = latitude - viewDistance;
        if (latitudeFrom < -90) {
            latitudeFrom = -90;
        }
        var latitudeTo = latitude + viewDistance;
        if (latitudeTo > 90) {
            latitudeTo = 90;
        }
        return new Envelope(new Coordinate(longitudeFrom, latitudeFrom),
                new Coordinate(longitudeTo, latitudeTo));
    }

    /**
     * Получить ключ - центр полигона lat + _ + lon
     */
    public static String getRowKey(Envelope terrainEnvelop) {
        return terrainEnvelop.centre().getX() + "_" + terrainEnvelop.centre().getY();
    }

    /**
     * Подготовка мапы нарезки сетки по долготе в зависимости от широты
     */
    private static void initResolutionLatLonStepMap() {
        for (var resolutionOption : Resolution.values()) {
            var latStep = resolutionOption.getTerrainOptions().getLatStep();
            var latStepMeters = LATITUDE_ONE_DEGREE_IN_METERS * latStep;
            //На экваторе шаг нарезки по долготе равен шагу нарезки по широте
            var lonStep = latStep;
            for (var lat = 0.0; lat < 90.0; lat += latStep) {
                var lonStepMeters = Geocentric.fromGeodetic(new Geodetic(lat, 0))
                        .distance(Geocentric.fromGeodetic(new Geodetic(lat, 1))) * lonStep;
                var doubling = false;
                //Т.к. основные высотные данные < 60 градусов, то чтобы не терять точность до этих широт не разбиваем
                if (resolutionOption.getTerrainOptions().getGenerationType() == TERRAIN_PLANET || lat > 60) {
                    if (lonStepMeters / latStepMeters < MIN_LON_LAT_ASPECT_RATIO && 360 % (lonStep * 2) == 0) {
                        lonStep *= 2;
                        doubling = true;
                    }
                }
                resolutionLatLonStepMap.computeIfAbsent(resolutionOption, k -> new HashMap<>())
                        .put(lat, new LonStepInfo(lonStep, doubling));
            }
        }
    }

    public record LonStepInfo(Double lonStep, Boolean doubling) {
    }

}
