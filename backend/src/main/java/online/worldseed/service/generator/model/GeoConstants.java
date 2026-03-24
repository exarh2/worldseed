package online.worldseed.generator.service.generator.model;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Параметры земного элипсойда WGS84
 * latitude - широта B в элиптических координатах
 * longitude - долгота L в элиптических координатах
 *
 * @see <a href="http://ru.wikipedia.org/wiki/Земной_эллипсоид">Земной эллипсоид</a>
 */
public interface GeoConstants {
    /**
     * Большая полуось (экваториальный радиус) эллипсоида, м
     */
    double EARTH_RADIUS_A = 6378137.0;
    /**
     * Обратная величина геометрическому (полярному) сжатию
     */
    double ONE_TO_F = 298.257223563;
    /**
     * Геометрическое (полярное) сжатие
     */
    double F = 1 / ONE_TO_F;
    /**
     * Малая полуось (полярный радиус), м
     */
    double EARTH_RADIUS_B = EARTH_RADIUS_A * (1 - F);
    /**
     * Полярный радиус кривизны поверхности, м
     */
    double POLAR_RADIUS_OF_CURVATURE = EARTH_RADIUS_A / (1 - F);
    /**
     * первый эксцентриситет
     */
    double FIRST_ECCENTRICITY = F * (2 - F);
    /**
     * второй эксцентриситет
     */
    double SECOND_ECCENTRICITY = FIRST_ECCENTRICITY / (1 - FIRST_ECCENTRICITY);
    /**
     * Расстояние в метрах между двумя точками отстоящими на один градус широты на экваторе
     */
    double LATITUDE_ONE_DEGREE_IN_METERS = 2 * Math.PI * EARTH_RADIUS_B / 360;
    /**
     * Проекция используемая при создании геометрии
     */
    int SRID_WGS_84 = 4326;
    /**
     * Фаобрикка создания геометрий по умолчанию
     */
    GeometryFactory GD_GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID_WGS_84);

    private void fake() {
    }
}
