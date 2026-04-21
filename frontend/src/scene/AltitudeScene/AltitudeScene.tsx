import React, {Suspense, useCallback, useEffect, useMemo, useRef, useState} from "react";
import {Canvas} from "@react-three/fiber";
import {Environment, FlyControls, OrbitControls, Stars, useGLTF} from "@react-three/drei";
import {useSelector} from "react-redux";
import type {RootState} from "../../store";
import type {SceneStateRequest} from "../../store/api/sceneApi";
import {config} from "../../config";
import {EARTH_RADIUS} from "../constants";
import {PerspectiveCamera} from "three";
import {mapCenterToCameraPosition} from "./altitudeCameraMath";
import {useAltitudeSceneState} from "./useAltitudeSceneState";

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
    const [useFlyControls, setUseFlyControls] = useState(false);
    const orbitControlsRef = useRef<any>(null);
    const cameraRef = useRef<PerspectiveCamera | null>(null);
    const isInitialCameraAppliedRef = useRef(false);

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

    const sceneState = useAltitudeSceneState(sceneRequest);
    const terrainUrls = (sceneState?.terrainPaths ?? []).map((terrainPath) => `${config.terrainsBaseUrl}/${terrainPath}`);
    const firstTerrainUrl = terrainUrls[0] ?? null;

    useEffect(() => {
        isInitialCameraAppliedRef.current = false;
    }, [firstTerrainUrl]);

    const applyInitialCameraFromFirstTerrain = useCallback(() => {
        if (isInitialCameraAppliedRef.current) {
            return;
        }
        const camera = cameraRef.current;
        if (!(camera instanceof PerspectiveCamera)) {
            return;
        }

        const cameraPosition = mapCenterToCameraPosition(mapView.center, 100);
        camera.position.set(cameraPosition.x, cameraPosition.y, cameraPosition.z);
        const controls = orbitControlsRef.current;
        if (controls) {
            controls.target.set(0, 0, 0);
            controls.update();
        }
        isInitialCameraAppliedRef.current = true;
    }, [mapView.center]);

    return (
        <div style={{position: "relative", width: "100%", height: "100%"}}>
            <button
                type="button"
                onClick={() => setUseFlyControls((prev) => !prev)}
                style={{
                    position: "absolute",
                    top: 12,
                    right: 12,
                    zIndex: 10,
                    padding: "6px 10px",
                    borderRadius: 6,
                    border: "1px solid #d1d5db",
                    background: "#ffffff",
                    cursor: "pointer"
                }}
            >
                {useFlyControls ? "Switch to OrbitControls" : "Switch to FlyControls"}
            </button>
            <Canvas
                shadows
                gl={{antialias: true}}
                onCreated={({camera}) => {
                    if (camera instanceof PerspectiveCamera) {
                        cameraRef.current = camera;
                    }
                }}
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
                {useFlyControls ? (
                    <FlyControls
                        makeDefault
                        movementSpeed={250000}
                        rollSpeed={0.5}
                    />
                ) : (
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
                )}
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

                <mesh position={[0, 0, 0]}>
                    <sphereGeometry args={[500000, 32, 32]}/>
                    <meshStandardMaterial color="#60a5fa"/>
                </mesh>
            </Canvas>
        </div>
    );
};
