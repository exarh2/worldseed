import React, {Suspense, useCallback, useEffect, useMemo, useRef, useState} from "react";
import {Canvas} from "@react-three/fiber";
import {Environment, OrbitControls, Stars, useGLTF} from "@react-three/drei";
import {useSelector} from "react-redux";
import type {RootState} from "../store";
import {
    type SceneStateRequest,
    type SceneStateResult,
    useGetSceneGenerationMutation,
    useLazyGetSceneStateQuery
} from "../store/api/sceneApi";
import {config} from "../config";
import {EARTH_RADIUS} from "./constants";
import {PerspectiveCamera} from "three";

const GENERATION_POLL_INTERVAL_MS = 1000;

const mergeTerrainPaths = (existingPaths: string[], incomingPaths: string[]): string[] => {
    if (incomingPaths.length === 0) {
        return existingPaths;
    }
    const uniquePaths = new Set(existingPaths);
    for (const path of incomingPaths) {
        uniquePaths.add(path);
    }
    return Array.from(uniquePaths);
};

const AltitudeTerrainModel: React.FC<{ url: string; onLoaded?: () => void }> = ({url, onLoaded}) => {
    const gltf = useGLTF(url);

    useEffect(() => {
        onLoaded?.();
    }, [onLoaded]);

    return <primitive object={gltf.scene}/>;
};

export const AltitudeScene: React.FC = () => {
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentTerrainOptions);
    const mapView = useSelector((state: RootState) => state.ui.mapView);
    const orbitControlsRef = useRef<any>(null);
    const isInitialCameraAppliedRef = useRef(false);
    const [sceneState, setSceneState] = useState<SceneStateResult | null>(null);
    const [triggerGetSceneState] = useLazyGetSceneStateQuery();
    const [getSceneGeneration] = useGetSceneGenerationMutation();
    const lastRenderedTerrainPathsRef = useRef<string[]>([]);

    const sceneRequest = useMemo<SceneStateRequest | null>(() => {
        if (!currentSceneTerrainOption) {
            return null;
        }
        return {
            resolution: currentSceneTerrainOption.resolution,
            longitude: mapView.center[0],
            latitude: mapView.center[1],
            terrainViewDistance:
                "maxTerrainViewDistance" in currentSceneTerrainOption
                    ? currentSceneTerrainOption.maxTerrainViewDistance
                    : 3
        };
    }, [currentSceneTerrainOption, mapView.center]);

    useEffect(() => {
        if (!sceneRequest) {
            setSceneState(null);
            return;
        }

        let isCancelled = false;
        let lastGenerationRequestAt = 0;
        const loadScene = async () => {
            try {
                const normalizeSceneState = (
                    response: Partial<SceneStateResult> | null | undefined,
                    previousTerrainPaths: string[]
                ): SceneStateResult => {
                    const incomingTerrainPaths = Array.isArray(response?.terrainPaths)
                        ? response.terrainPaths
                        : [];
                    return {
                        terrainPaths: mergeTerrainPaths(previousTerrainPaths, incomingTerrainPaths),
                        waitingRowKeys: Array.isArray(response?.waitingRowKeys) ? response.waitingRowKeys : []
                    };
                };

                let nextSceneState = normalizeSceneState(
                    await triggerGetSceneState(sceneRequest, false).unwrap(),
                    lastRenderedTerrainPathsRef.current
                );
                if (isCancelled) {
                    return;
                }
                setSceneState(nextSceneState);
                if (nextSceneState.terrainPaths.length > 0) {
                    lastRenderedTerrainPathsRef.current = nextSceneState.terrainPaths;
                }

                while (!isCancelled && nextSceneState.waitingRowKeys.length > 0) {
                    const now = Date.now();
                    const elapsed = now - lastGenerationRequestAt;
                    const waitMs = Math.max(0, GENERATION_POLL_INTERVAL_MS - elapsed);
                    if (waitMs > 0) {
                        await new Promise((resolve) => {
                            window.setTimeout(resolve, waitMs);
                        });
                        if (isCancelled) {
                            return;
                        }
                    }

                    const generationState = await getSceneGeneration({
                        waitingRowKeys: nextSceneState.waitingRowKeys
                    }).unwrap();
                    lastGenerationRequestAt = Date.now();
                    if (isCancelled) {
                        return;
                    }

                    nextSceneState = normalizeSceneState(generationState, nextSceneState.terrainPaths);
                    setSceneState(nextSceneState);
                    if (nextSceneState.terrainPaths.length > 0) {
                        lastRenderedTerrainPathsRef.current = nextSceneState.terrainPaths;
                    }
                }
            } catch {
                if (!isCancelled) {
                    setSceneState((prev) => prev);
                }
            }
        };

        void loadScene();

        return () => {
            isCancelled = true;
        };
    }, [sceneRequest, triggerGetSceneState, getSceneGeneration]);

    const terrainUrls = (sceneState?.terrainPaths ?? []).map((terrainPath) => `${config.terrainsBaseUrl}/${terrainPath}`);
    const firstTerrainUrl = terrainUrls[0] ?? null;

    useEffect(() => {
        isInitialCameraAppliedRef.current = false;
    }, [firstTerrainUrl]);

    const applyInitialCameraFromFirstTerrain = useCallback(() => {
        if (isInitialCameraAppliedRef.current) {
            return;
        }
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
        const distanceToCenter = EARTH_RADIUS + 100;

        const x = distanceToCenter * Math.cos(latitude) * Math.cos(longitude);
        const y = distanceToCenter * Math.cos(latitude) * Math.sin(longitude);
        const z = distanceToCenter * Math.sin(latitude);

        camera.position.set(x, y, z);
        controls.target.set(0, 0, 0);
        controls.update();
        isInitialCameraAppliedRef.current = true;
    }, [mapView.center]);

    return (
        <Canvas
            shadows
            gl={{antialias: true}}
            camera={{fov: 35, near: 10, far: 135504085, position: [EARTH_RADIUS * 1.1, 0, 0], up: [0, 0, 1]}}
            style={{background: "#f3f4f6"}}
        >
            <color attach="background" args={["#ffffff"]}/>
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
                rotateSpeed={0.35}
                zoomSpeed={0.5}
                panSpeed={0.5}
                minDistance={EARTH_RADIUS}
                maxDistance={50000000}
                screenSpacePanning
                target={[0, 0, 0]}
            />
            <ambientLight intensity={0.35}/>
            <directionalLight position={[5, 10, 8]} intensity={1.1}/>
            <Environment preset="city"/>
            <Suspense fallback={null}>
                {terrainUrls.map((terrainUrl, index) => (
                    <AltitudeTerrainModel
                        key={terrainUrl}
                        url={terrainUrl}
                        onLoaded={index === 0 ? applyInitialCameraFromFirstTerrain : undefined}
                    />
                ))}
            </Suspense>

            {/* Altitude scene placeholder: temporary simple marker in scene center */}
            <mesh position={[0, 0, 0]}>
                <sphereGeometry args={[500000, 32, 32]}/>
                <meshStandardMaterial color="#60a5fa"/>
            </mesh>
        </Canvas>
    );
};
