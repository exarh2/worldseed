import {useEffect, useRef} from "react";
import {PerspectiveCamera} from "three";
import type {OrbitControls as OrbitControlsImpl} from "three-stdlib";
import type {MapViewState} from "../../store/slices/uiSlice";
import {cameraPositionToMapView, mapViewToCameraPosition} from "./planetCameraMath";

const MAP_VIEW_DISPATCH_THROTTLE_MS = 300;

interface OrbitControlsChangeEvent {
    target?: {
        object?: unknown;
    };
}

interface UsePlanetMapViewSyncParams {
    mapView: MapViewState;
    onMapViewChange: (nextMapView: MapViewState) => void;
}

interface UsePlanetMapViewSyncResult {
    orbitControlsRef: React.RefObject<OrbitControlsImpl | null>;
    onControlsChange: (event?: OrbitControlsChangeEvent) => void;
    onControlsStart: () => void;
    onControlsEnd: () => void;
}

const isEqualMapView = (a: MapViewState, b: MapViewState): boolean =>
    a.center[0] === b.center[0] && a.center[1] === b.center[1] && a.zoom === b.zoom;

export const usePlanetMapViewSync = ({mapView, onMapViewChange}: UsePlanetMapViewSyncParams): UsePlanetMapViewSyncResult => {
    const orbitControlsRef = useRef<OrbitControlsImpl>(null);
    const lastMapViewRef = useRef<MapViewState | null>(null);
    const isApplyingMapViewRef = useRef(false);
    const isUserInteractingRef = useRef(false);
    const lastMapViewDispatchAtRef = useRef(0);
    const pendingMapViewRef = useRef<MapViewState | null>(null);
    const trailingDispatchTimeoutRef = useRef<number | null>(null);

    const dispatchMapViewThrottled = (nextMapView: MapViewState, forceImmediate = false) => {
        const now = Date.now();
        const elapsed = now - lastMapViewDispatchAtRef.current;
        const canDispatchNow = forceImmediate || elapsed >= MAP_VIEW_DISPATCH_THROTTLE_MS;

        if (canDispatchNow) {
            if (trailingDispatchTimeoutRef.current !== null) {
                window.clearTimeout(trailingDispatchTimeoutRef.current);
                trailingDispatchTimeoutRef.current = null;
            }
            pendingMapViewRef.current = null;
            lastMapViewDispatchAtRef.current = now;
            lastMapViewRef.current = nextMapView;
            onMapViewChange(nextMapView);
            return;
        }

        pendingMapViewRef.current = nextMapView;
        if (trailingDispatchTimeoutRef.current !== null) {
            return;
        }

        const waitMs = MAP_VIEW_DISPATCH_THROTTLE_MS - elapsed;
        trailingDispatchTimeoutRef.current = window.setTimeout(() => {
            trailingDispatchTimeoutRef.current = null;
            if (!pendingMapViewRef.current) {
                return;
            }
            const pending = pendingMapViewRef.current;
            pendingMapViewRef.current = null;
            lastMapViewDispatchAtRef.current = Date.now();
            lastMapViewRef.current = pending;
            onMapViewChange(pending);
        }, waitMs);
    };

    useEffect(() => () => {
        if (trailingDispatchTimeoutRef.current !== null) {
            window.clearTimeout(trailingDispatchTimeoutRef.current);
            trailingDispatchTimeoutRef.current = null;
        }
    }, []);

    useEffect(() => {
        const controls = orbitControlsRef.current;
        if (!controls) {
            return;
        }

        const camera = controls.object;
        if (!(camera instanceof PerspectiveCamera)) {
            return;
        }

        const cameraPosition = mapViewToCameraPosition(mapView, camera.fov, window.innerHeight);

        isApplyingMapViewRef.current = true;
        camera.position.set(cameraPosition.x, cameraPosition.y, cameraPosition.z);
        controls.target.set(0, 0, 0);
        controls.update();
        lastMapViewRef.current = mapView;
        isApplyingMapViewRef.current = false;
    }, [mapView]);

    const onControlsChange = (event?: OrbitControlsChangeEvent) => {
        if (isApplyingMapViewRef.current || !isUserInteractingRef.current || !event) {
            return;
        }

        const camera = event.target?.object;
        if (!(camera instanceof PerspectiveCamera)) {
            return;
        }

        const nextMapView = cameraPositionToMapView(camera.position, camera.fov, window.innerHeight);
        if (!nextMapView) {
            return;
        }

        if (!lastMapViewRef.current || !isEqualMapView(lastMapViewRef.current, nextMapView)) {
            dispatchMapViewThrottled(nextMapView);
        }
    };

    const onControlsStart = () => {
        isUserInteractingRef.current = true;
    };

    const onControlsEnd = () => {
        isUserInteractingRef.current = false;
        if (pendingMapViewRef.current) {
            dispatchMapViewThrottled(pendingMapViewRef.current, true);
        }
    };

    return {
        orbitControlsRef,
        onControlsChange,
        onControlsStart,
        onControlsEnd
    };
};
