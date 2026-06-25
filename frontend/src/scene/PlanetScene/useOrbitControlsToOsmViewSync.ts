import {useEffect, useRef} from "react";
import {PerspectiveCamera} from "three";
import type {OrbitControls as OrbitControlsImpl} from "three-stdlib";
import {OsmViewState, setOsmViewState} from "../../store/slices/uiSlice";
import {cameraPositionToOsmViewState, osmViewStateToCameraPosition} from "./planetCameraMath";
import {useDispatch, useSelector} from "react-redux";
import type {AppDispatch, RootState} from "../../store";
import {EARTH_RADIUS} from "../../utils/constants";

const MAP_VIEW_DISPATCH_THROTTLE_MS = 300;

interface OrbitControlsChangeEvent {
    target?: {
        object?: unknown;
    };
}

interface UsePlanetOsmViewSyncResult {
    orbitControlsRef: React.RefObject<OrbitControlsImpl | null>;
    onControlsChange: (event?: OrbitControlsChangeEvent) => void;
    onControlsStart: () => void;
    onControlsEnd: () => void;
}

const isEqualMapView = (a: OsmViewState, b: OsmViewState): boolean =>
    a.lon === b.lon && a.lat === b.lat && a.zoom === b.zoom;

export const useOrbitControlsToOsmViewSync = (): UsePlanetOsmViewSyncResult => {
    const orbitControlsRef = useRef<OrbitControlsImpl>(null);
    const osmViewState = useSelector((state: RootState) => state.ui.osmViewState);

    const dispatch = useDispatch<AppDispatch>();
    const onMapViewChange = (nextOsmViewState: OsmViewState) => {
        dispatch(setOsmViewState(nextOsmViewState));
    }
    
    const lastMapViewRef = useRef<OsmViewState | null>(null);
    const isApplyingMapViewRef = useRef(false);
    const isUserInteractingRef = useRef(false);
    const lastMapViewDispatchAtRef = useRef(0);
    const pendingMapViewRef = useRef<OsmViewState | null>(null);
    const trailingDispatchTimeoutRef = useRef<number | null>(null);

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

        const cameraPosition = osmViewStateToCameraPosition(osmViewState);
        console.log("fffffffffffffffffff", cameraPosition, (cameraPosition.x / EARTH_RADIUS))

        isApplyingMapViewRef.current = true;
        camera.position.set(cameraPosition.x, cameraPosition.y, cameraPosition.z);
        controls.target.set(0, 0, 0);
        controls.update();
        lastMapViewRef.current = osmViewState;
        isApplyingMapViewRef.current = false;
    }, [osmViewState]);

    const onControlsChange = (event?: OrbitControlsChangeEvent) => {
        if (isApplyingMapViewRef.current || !isUserInteractingRef.current || !event) {
            return;
        }

        const camera = event.target?.object;
        if (!(camera instanceof PerspectiveCamera)) {
            return;
        }

        const nextOsmViewState = cameraPositionToOsmViewState(camera.position);
        if (!nextOsmViewState) {
            return;
        }

        if (!lastMapViewRef.current || !isEqualMapView(lastMapViewRef.current, nextOsmViewState)) {
            dispatchOsmViewThrottled(nextOsmViewState);
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

    const dispatchOsmViewThrottled = (nextOsmViewState: OsmViewState, forceImmediate = false) => {
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
            lastMapViewRef.current = nextOsmViewState;
            onMapViewChange(nextOsmViewState);
            return;
        }

        pendingMapViewRef.current = nextOsmViewState;
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

    return {
        orbitControlsRef,
        onControlsChange,
        onControlsStart,
        onControlsEnd
    };
};
