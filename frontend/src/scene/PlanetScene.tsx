import React, {Suspense} from "react";
import {useSelector} from "react-redux";
import type {RootState} from "../store";
import {Canvas} from "@react-three/fiber";
import {Bounds, Environment, OrbitControls, useGLTF} from "@react-three/drei";
import {config} from "../config";
import {useGetPlanetSceneQuery} from "../store/api/sceneApi";

const PlanetTerrainModel: React.FC<{ url: string }> = ({url}) => {
    const gltf = useGLTF(url);
    return <primitive object={gltf.scene}/>;
};
// [-3282059.946, -2327411.335, 25504085.127],
export const PlanetScene: React.FC = () => {
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentSceneTerrainOptions);
    const {data} = useGetPlanetSceneQuery(currentSceneTerrainOption?.resolution ?? "R_9", {
        skip: !currentSceneTerrainOption?.resolution
    });
    const terrainUrl = data?.terrainPath
        ? `${config.terrainsBaseUrl}/${data.terrainPath}`
        : null;

    return (
        <Canvas
            shadows
            gl={{antialias: true}}
            camera={{fov: 35, near: 0.01, far: 135504085, position: [25504085, 0, 0], up: [0, 0, 1]}}
            style={{background: "#f3f4f6"}}
        >
            <OrbitControls
                makeDefault
                autoRotate={false}
                enableDamping
                dampingFactor={0.08}
                minDistance={0.1}
                maxDistance={50000000}
                screenSpacePanning
                target={[0, 0, 0]}
            />
            <axesHelper args={[25504085]}/>
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
