import React, {Suspense} from "react";
import {useDispatch, useSelector} from "react-redux";
import type {AppDispatch, RootState} from "../../store";
import {Canvas} from "@react-three/fiber";
import {Environment, OrbitControls, Stars, useGLTF} from "@react-three/drei";
import {config} from "../../config";
import {useGetPlanetSceneQuery} from "../../store/api/sceneApi";
import {setMapView} from "../../store/slices/uiSlice";
import {EARTH_RADIUS} from "../constants";
import {usePlanetMapViewSync} from "./usePlanetMapViewSync";

const PlanetTerrainModel: React.FC<{ url: string }> = ({url}) => {
    const gltf = useGLTF(url);
    return <primitive object={gltf.scene}/>;
};

export const PlanetScene: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentTerrainOptions);
    const mapView = useSelector((state: RootState) => state.ui.mapView);
    const {data} = useGetPlanetSceneQuery(currentSceneTerrainOption!.resolution);
    const terrainUrl = data?.terrainPath
        ? `${config.terrainsBaseUrl}/${data.terrainPath}`
        : null;

    const {orbitControlsRef, onControlsChange, onControlsStart, onControlsEnd} = usePlanetMapViewSync({
        mapView,
        onMapViewChange: (nextMapView) => {
            dispatch(setMapView(nextMapView));
        }
    });

    return (
        <Canvas
            shadows
            gl={{antialias: true}}
            camera={{fov: 35, near: 10, far: 135504085, position: [EARTH_RADIUS * 4, 0, 0], up: [0, 0, 1]}}
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
                maxDistance={EARTH_RADIUS * 10}
                screenSpacePanning
                target={[0, 0, 0]}
                onChange={onControlsChange}
                onStart={onControlsStart}
                onEnd={onControlsEnd}
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
