import {
    EARTH_RADIUS_A,
    EARTH_RADIUS_B,
    F,
    FIRST_ECCENTRICITY,
    POLAR_RADIUS_OF_CURVATURE,
    SECOND_ECCENTRICITY
} from "./constants";

export interface Geocentric {
    x: number;
    y: number;
    z: number;
}

export interface Geodetic {
    lat: number;
    lon: number;
    alt: number;
}

export const geocentricFromGeodetic = (geodetic: Geodetic): Geocentric => {
    const altitude = geodetic.alt;
    const latRad = geodetic.lat * Math.PI / 180;
    const lonRad = geodetic.lon * Math.PI / 180;
    const cosB = Math.cos(latRad);
    const sinB = Math.sin(latRad);
    const p = (EARTH_RADIUS_A * EARTH_RADIUS_A) /
        Math.sqrt(
            (EARTH_RADIUS_A * EARTH_RADIUS_A) * (cosB * cosB) +
            (EARTH_RADIUS_B * EARTH_RADIUS_B) * (sinB * sinB)
        );
    const x = (p + altitude) * cosB * Math.cos(lonRad);
    const y = (p + altitude) * cosB * Math.sin(lonRad);
    const z = (((EARTH_RADIUS_B * EARTH_RADIUS_B) / (EARTH_RADIUS_A * EARTH_RADIUS_A)) * p + altitude) * sinB;
    return {x, y, z};
};

export const geodeticFromGeocentric = (gc: Geocentric): Geodetic => {
    const p = Math.hypot(gc.x, gc.y);
    if (p === 0) {
        const lat = gc.z >= 0 ? Math.PI / 2 : -Math.PI / 2;
        const lon = 0;
        const h = Math.abs(gc.z) - EARTH_RADIUS_B;
        return {lat: lat * 180 / Math.PI, lon: lon * 180 / Math.PI, alt: h};
    }

    let t = gc.z / p * (1 + (SECOND_ECCENTRICITY * EARTH_RADIUS_B) / Math.hypot(p, gc.z));
    t = t * (1 - F);
    const latIter = Math.atan(t);
    const cosLatIter = Math.cos(latIter);
    const sinLatIter = Math.sin(latIter);
    t = (gc.z + SECOND_ECCENTRICITY * EARTH_RADIUS_B * Math.pow(sinLatIter, 3)) /
        (p - FIRST_ECCENTRICITY * EARTH_RADIUS_A * Math.pow(cosLatIter, 3));

    const lon = Math.atan2(gc.y, gc.x);
    const lat = Math.atan(t);
    const cosLat = Math.cos(lat);
    const n = POLAR_RADIUS_OF_CURVATURE / Math.sqrt(1 + SECOND_ECCENTRICITY * Math.pow(cosLat, 2));
    const h = Math.abs(t) <= 1
        ? (p / cosLat - n)
        : (gc.z / Math.sin(lat) - n * (1 - FIRST_ECCENTRICITY));

    return {lat: lat * 180 / Math.PI, lon: lon * 180 / Math.PI, alt: h};
};
