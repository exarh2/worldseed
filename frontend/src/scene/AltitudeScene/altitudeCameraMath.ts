import {EARTH_RADIUS} from "../constants";

export interface Vec3 {
    x: number;
    y: number;
    z: number;
}

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
