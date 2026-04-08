import React, {Suspense} from "react";
import {useSelector} from "react-redux";
import type {RootState} from "../store";
import {Canvas} from "@react-three/fiber";
import {Bounds, Environment, OrbitControls, Stars, useGLTF} from "@react-three/drei";
import {config} from "../config";
import {useGetPlanetSceneQuery} from "../store/api/sceneApi";
import {Resolution} from "../store/slices/sceneSlice";
import {EARTH_RADIUS} from "./constants";

const PlanetTerrainModel: React.FC<{ url: string }> = ({url}) => {
    const gltf = useGLTF(url);
    return <primitive object={gltf.scene}/>;
};
// [-3282059.946, -2327411.335, 25504085.127],
export const PlanetScene: React.FC = () => {
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentTerrainOptions);
    const {data} = useGetPlanetSceneQuery(Resolution.R_3, {
        skip: !currentSceneTerrainOption?.resolution
    });
    const terrainUrl = data?.terrainPath
        ? `${config.terrainsBaseUrl}/${data.terrainPath}`
        : null;

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
                makeDefault
                autoRotate={false}
                enableDamping
                dampingFactor={0.08}
                minDistance={EARTH_RADIUS}
                maxDistance={50000000}
                screenSpacePanning
                target={[0, 0, 0]}
            />
            <ambientLight intensity={0.35}/>
            <directionalLight position={[5, 10, 8]} intensity={1.1}/>
            <Environment preset="city"/>
            <Suspense fallback={null}>
                {terrainUrl && (
                    // <Bounds fit clip observe margin={1.2}>
                        <PlanetTerrainModel url={terrainUrl}/>
                    // </Bounds>
                )}
            </Suspense>
        </Canvas>
    );
};
