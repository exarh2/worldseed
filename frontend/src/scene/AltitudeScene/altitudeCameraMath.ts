import type {MapViewState} from "../../store/slices/uiSlice";
import {EARTH_RADIUS} from "../../utils/constants";

export interface Vec3 {
    x: number;
    y: number;
    z: number;
}

const MAP_CENTER_PRECISION = 6;
const MAP_ZOOM_PRECISION = 3;
const EARTH_CIRCUMFERENCE_M = 40075016.686;
const MIN_MAP_ZOOM = 0;
const MAX_MAP_ZOOM = 19;

const clamp = (value: number, min: number, max: number): number => Math.min(max, Math.max(min, value));

const roundToPrecision = (value: number, precision: number): number => {
    const factor = 10 ** precision;
    return Math.round(value * factor) / factor;
};

export const mapCenterToCameraPosition = (center: [number, number], altitude: number): Vec3 => {
    const longitude = center[0] * Math.PI / 180;
    const latitude = center[1] * Math.PI / 180;
    const distanceToCenter = EARTH_RADIUS + altitude;

    return {
        x: distanceToCenter * Math.cos(latitude) * Math.cos(longitude),
        y: distanceToCenter * Math.cos(latitude) * Math.sin(longitude),
        z: distanceToCenter * Math.sin(latitude)
    };
};

export const cameraPositionToMapView = (
    position: Vec3,
    cameraFovDegrees: number,
    viewportHeight: number
): MapViewState | null => {
    const distanceToCenter = Math.sqrt(position.x ** 2 + position.y ** 2 + position.z ** 2);
    if (distanceToCenter <= 0) {
        return null;
    }

    const latitude = Math.asin(clamp(position.z / distanceToCenter, -1, 1)) * 180 / Math.PI;
    const longitude = Math.atan2(position.y, position.x) * 180 / Math.PI;
    const altitude = Math.max(distanceToCenter - EARTH_RADIUS, 1);
    const safeViewportHeight = Math.max(viewportHeight, 1);
    const fovRad = (cameraFovDegrees * Math.PI) / 180;
    const metersPerPixel = (2 * altitude * Math.tan(fovRad / 2)) / safeViewportHeight;
    const latitudeCos = Math.max(Math.cos(latitude * Math.PI / 180), 1e-6);
    const zoom = Math.log2((EARTH_CIRCUMFERENCE_M * latitudeCos) / (256 * Math.max(metersPerPixel, 1e-9)));

    return {
        center: [
            roundToPrecision(longitude, MAP_CENTER_PRECISION),
            roundToPrecision(latitude, MAP_CENTER_PRECISION)
        ],
        zoom: roundToPrecision(clamp(zoom, MIN_MAP_ZOOM, MAX_MAP_ZOOM), MAP_ZOOM_PRECISION)
    };
};
