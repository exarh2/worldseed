import {useEffect, useRef} from "react";
import {PerspectiveCamera} from "three";
import type {OrbitControls as OrbitControlsImpl} from "three-stdlib";
import type {OsmViewState} from "../../store/slices/uiSlice";
import {cameraPositionToMapView} from "./altitudeCameraMath";

const MAP_VIEW_DISPATCH_THROTTLE_MS = 300;

interface OrbitControlsChangeEvent {
    target?: {
        object?: unknown;
    };
}

interface UseAltitudeMapViewSyncParams {
    onMapViewChange: (nextMapView: OsmViewState) => void;
}

interface UseAltitudeMapViewSyncResult {
    orbitControlsRef: React.RefObject<OrbitControlsImpl | null>;
    onControlsChange: (event?: OrbitControlsChangeEvent) => void;
    onControlsStart: () => void;
    onControlsEnd: () => void;
}

const isEqualMapView = (a: OsmViewState, b: OsmViewState): boolean =>
    a.lon === b.lon && a.lat === b.lat && a.zoom === b.zoom;

export const useAltitudeMapViewSync = ({onMapViewChange}: UseAltitudeMapViewSyncParams): UseAltitudeMapViewSyncResult => {
    const orbitControlsRef = useRef<OrbitControlsImpl>(null);
    const lastMapViewRef = useRef<OsmViewState | null>(null);
    const isUserInteractingRef = useRef(false);
    const lastMapViewDispatchAtRef = useRef(0);
    const pendingMapViewRef = useRef<OsmViewState | null>(null);
    const trailingDispatchTimeoutRef = useRef<number | null>(null);

    const dispatchOsmViewThrottled = (nextMapView: OsmViewState, forceImmediate = false) => {
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

    const onControlsChange = (event?: OrbitControlsChangeEvent) => {
        if (!isUserInteractingRef.current || !event) {
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
            dispatchOsmViewThrottled(nextMapView);
        }
    };

    const onControlsStart = () => {
        isUserInteractingRef.current = true;
    };

    const onControlsEnd = () => {
        isUserInteractingRef.current = false;
        if (pendingMapViewRef.current) {
            dispatchOsmViewThrottled(pendingMapViewRef.current, true);
        }
    };

    return {
        orbitControlsRef,
        onControlsChange,
        onControlsStart,
        onControlsEnd
    };
};
