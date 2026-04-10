import React, {Suspense, useEffect, useRef} from "react";
import {useDispatch, useSelector} from "react-redux";
import type {AppDispatch, RootState} from "../store";
import {Canvas} from "@react-three/fiber";
import {Environment, OrbitControls, Stars, useGLTF} from "@react-three/drei";
import {PerspectiveCamera} from "three";
import {config} from "../config";
import {useGetPlanetSceneQuery} from "../store/api/sceneApi";
import {EARTH_RADIUS} from "./constants";
import {setMapView, type MapViewState} from "../store/slices/uiSlice";

const PlanetTerrainModel: React.FC<{ url: string }> = ({url}) => {
    const gltf = useGLTF(url);
    return <primitive object={gltf.scene}/>;
};

const MIN_MAP_ZOOM = 0;
const MAX_MAP_ZOOM = 19;
const MAP_CENTER_PRECISION = 6;
const MAP_ZOOM_PRECISION = 3;
const EARTH_CIRCUMFERENCE_M = 40075016.686;
const MAP_VIEW_DISPATCH_THROTTLE_MS = 300;

const roundToPrecision = (value: number, precision: number): number => {
    const factor = 10 ** precision;
    return Math.round(value * factor) / factor;
};

const clamp = (value: number, min: number, max: number): number => Math.min(max, Math.max(min, value));

export const PlanetScene: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentTerrainOptions);
    const mapView = useSelector((state: RootState) => state.ui.mapView);
    const {data} = useGetPlanetSceneQuery(currentSceneTerrainOption!.resolution);
    const lastMapViewRef = useRef<MapViewState | null>(null);
    const orbitControlsRef = useRef<any>(null);
    const isApplyingMapViewRef = useRef(false);
    const isUserInteractingRef = useRef(false);
    const lastMapViewDispatchAtRef = useRef(0);
    const pendingMapViewRef = useRef<MapViewState | null>(null);
    const trailingDispatchTimeoutRef = useRef<number | null>(null);
    const terrainUrl = data?.terrainPath
        ? `${config.terrainsBaseUrl}/${data.terrainPath}`
        : null;

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
            dispatch(setMapView(nextMapView));
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
            dispatch(setMapView(pending));
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

        const longitude = mapView.center[0] * Math.PI / 180;
        const latitude = mapView.center[1] * Math.PI / 180;
        const latitudeCos = Math.max(Math.cos(latitude), 1e-6);
        const viewportHeight = Math.max(window.innerHeight, 1);
        const fovRad = (camera.fov * Math.PI) / 180;
        const metersPerPixel =
            (EARTH_CIRCUMFERENCE_M * latitudeCos) / (256 * Math.pow(2, clamp(mapView.zoom, MIN_MAP_ZOOM, MAX_MAP_ZOOM)));
        const altitude = (metersPerPixel * viewportHeight) / (2 * Math.tan(fovRad / 2));
        const distanceToCenter = clamp(EARTH_RADIUS + altitude, EARTH_RADIUS, 50000000);

        const x = distanceToCenter * Math.cos(latitude) * Math.cos(longitude);
        const y = distanceToCenter * Math.cos(latitude) * Math.sin(longitude);
        const z = distanceToCenter * Math.sin(latitude);

        isApplyingMapViewRef.current = true;
        camera.position.set(x, y, z);
        controls.target.set(0, 0, 0);
        controls.update();
        lastMapViewRef.current = mapView;
        isApplyingMapViewRef.current = false;
    }, [mapView]);

    return (
        <Canvas
            shadows
            gl={{antialias: true}}
            camera={{fov: 35, near: /*0.01*/10, far: 135504085, position: [25504085, 0, 0], up: [0, 0, 1]}}
            style={{background: "#f3f4f6"}}
        >
            <color attach="background" args={["#030712"]}/>
            <Stars
                radius={120000000}
                depth={60000000}
                count={5000}
                factor={6}
                saturation={0}
                speed={0.5}
            />
            <OrbitControls
                ref={orbitControlsRef}
                makeDefault
                autoRotate={false}
                enableDamping
                dampingFactor={0.08}
                minDistance={EARTH_RADIUS}
                maxDistance={50000000}
                screenSpacePanning
                target={[0, 0, 0]}
                onChange={(event) => {
                    if (isApplyingMapViewRef.current) {
                        return;
                    }
                    if (!isUserInteractingRef.current) {
                        return;
                    }
                    if (!event) {
                        return;
                    }
                    const controls = event.target;
                    const camera = controls.object;
                    if (!(camera instanceof PerspectiveCamera)) {
                        return;
                    }
                    const position = camera.position;
                    const distanceToCenter = position.length();
                    if (distanceToCenter <= 0) {
                        return;
                    }

                    const latitude = Math.asin(clamp(position.z / distanceToCenter, -1, 1)) * 180 / Math.PI;
                    const longitude = Math.atan2(position.y, position.x) * 180 / Math.PI;

                    const altitude = Math.max(distanceToCenter - EARTH_RADIUS, 1);
                    const viewportHeight = Math.max(window.innerHeight, 1);
                    const fovRad = (camera.fov * Math.PI) / 180;
                    const metersPerPixel = (2 * altitude * Math.tan(fovRad / 2)) / viewportHeight;
                    const latitudeCos = Math.max(Math.cos(latitude * Math.PI / 180), 1e-6);
                    const zoom =
                        Math.log2((EARTH_CIRCUMFERENCE_M * latitudeCos) / (256 * Math.max(metersPerPixel, 1e-9)));

                    const nextMapView: MapViewState = {
                        center: [
                            roundToPrecision(longitude, MAP_CENTER_PRECISION),
                            roundToPrecision(latitude, MAP_CENTER_PRECISION)
                        ],
                        zoom: roundToPrecision(clamp(zoom, MIN_MAP_ZOOM, MAX_MAP_ZOOM), MAP_ZOOM_PRECISION)
                    };

                    if (
                        !lastMapViewRef.current ||
                        lastMapViewRef.current.center[0] !== nextMapView.center[0] ||
                        lastMapViewRef.current.center[1] !== nextMapView.center[1] ||
                        lastMapViewRef.current.zoom !== nextMapView.zoom
                    ) {
                        dispatchMapViewThrottled(nextMapView);
                    }
                }}
                onStart={() => {
                    isUserInteractingRef.current = true;
                }}
                onEnd={() => {
                    isUserInteractingRef.current = false;
                    if (pendingMapViewRef.current) {
                        dispatchMapViewThrottled(pendingMapViewRef.current, true);
                    }
                }}
            />
            <ambientLight intensity={0.35}/>
            <directionalLight position={[5, 10, 8]} intensity={1.1}/>
            <Environment preset="city"/>
            <Suspense fallback={null}>
                {terrainUrl && (
                    <PlanetTerrainModel url={terrainUrl}/>
                )}
            </Suspense>
        </Canvas>
    );
};
