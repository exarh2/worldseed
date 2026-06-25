import type {OsmViewState} from "../../store/slices/uiSlice";
import {EARTH_RADIUS} from "../../utils/constants";
import {geocentricFromGeodetic, geodeticFromGeocentric} from "../../utils/geomath";

export const MIN_MAP_ZOOM = 0;
export const MAX_MAP_ZOOM = 19;
export const MAP_CENTER_PRECISION = 6;
export const MAP_ZOOM_PRECISION = 3;
export const EARTH_CIRCUMFERENCE_M = 40075016.686;
export const PLANET_CAMERA_FOV_DEGREES = 35;
/** Halves camera altitude so the planet appears ~2× closer at the same OSM zoom. */
const PLANET_CAMERA_DISTANCE_SCALE = 0.4;

export interface Vec3 {
    x: number;
    y: number;
    z: number;
}

export const roundToPrecision = (value: number, precision: number): number => {
    const factor = 10 ** precision;
    return Math.round(value * factor) / factor;
};

export const clamp = (value: number, min: number, max: number): number => Math.min(max, Math.max(min, value));

export const osmViewStateToCameraPosition = (mapView: OsmViewState): Vec3 => {
    const longitude = mapView.lon;
    const latitude = mapView.lat;
    //const latitudeCos = Math.max(Math.cos(latitude * Math.PI / 180), 1e-6);
    const latitudeCos = 1;
    const safeViewportHeight = Math.max(window.innerHeight, 1);
    const fovRad = (PLANET_CAMERA_FOV_DEGREES * Math.PI) / 180;
    const metersPerPixel =
        (EARTH_CIRCUMFERENCE_M * latitudeCos) /
        (256 * Math.pow(2, clamp(mapView.zoom, MIN_MAP_ZOOM, MAX_MAP_ZOOM)));
    const rawAltitude =
        ((metersPerPixel * safeViewportHeight) / (2 * Math.tan(fovRad / 2))) * PLANET_CAMERA_DISTANCE_SCALE;
    const geocentric = geocentricFromGeodetic({lat: latitude, lon: longitude, alt: rawAltitude});
    const geodetic = geodeticFromGeocentric(geocentric);
    const altitude = clamp(geodetic.alt, 0, EARTH_RADIUS * 9);
    return geocentricFromGeodetic({lat: geodetic.lat, lon: geodetic.lon, alt: altitude});
};

export const cameraPositionToOsmViewState = (position: Vec3): OsmViewState | null => {
    const distanceToCenter = Math.sqrt(position.x ** 2 + position.y ** 2 + position.z ** 2);
    if (distanceToCenter <= 0) {
        return null;
    }

    const geodetic = geodeticFromGeocentric(position);
    var latitude = geodetic.lat;
    const longitude = geodetic.lon;
    const altitude = Math.max(geodetic.alt, 1);
    const effectiveAltitude = altitude / PLANET_CAMERA_DISTANCE_SCALE;
    const safeViewportHeight = Math.max(window.innerHeight, 1);
    const fovRad = (PLANET_CAMERA_FOV_DEGREES * Math.PI) / 180;
    const metersPerPixel = (2 * effectiveAltitude * Math.tan(fovRad / 2)) / safeViewportHeight;
    //const latitudeCos = Math.max(Math.cos(latitude * Math.PI / 180), 1e-6);
    const latitudeCos = 1;
    const zoom = Math.log2((EARTH_CIRCUMFERENCE_M * latitudeCos) / (256 * Math.max(metersPerPixel, 1e-9)));
    console.log('xxx', latitude)
    return {
        lon: roundToPrecision(longitude, MAP_CENTER_PRECISION),
        lat: roundToPrecision(latitude, MAP_CENTER_PRECISION),
        zoom: roundToPrecision(clamp(zoom, MIN_MAP_ZOOM, MAX_MAP_ZOOM), MAP_ZOOM_PRECISION)
    };
};
