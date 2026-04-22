/**
 * Большая полуось (экваториальный радиус) эллипсоида WGS84, м.
 */
export const EARTH_RADIUS_A = 6_378_137.0;

/**
 * Обратная величина геометрическому (полярному) сжатию.
 */
export const ONE_TO_F = 298.257223563;

/**
 * Геометрическое (полярное) сжатие.
 */
export const F = 1 / ONE_TO_F;

/**
 * Малая полуось (полярный радиус), м.
 */
export const EARTH_RADIUS_B = EARTH_RADIUS_A * (1 - F);

/**
 * Полярный радиус кривизны поверхности, м.
 */
export const POLAR_RADIUS_OF_CURVATURE = EARTH_RADIUS_A / (1 - F);

/**
 * Первый эксцентриситет.
 */
export const FIRST_ECCENTRICITY = F * (2 - F);

/**
 * Второй эксцентриситет.
 */
export const SECOND_ECCENTRICITY = FIRST_ECCENTRICITY / (1 - FIRST_ECCENTRICITY);

/**
 * Совместимый алиас текущей константы радиуса Земли в сценах.
 */
export const EARTH_RADIUS = EARTH_RADIUS_A;
