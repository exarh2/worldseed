import React, {Suspense} from "react";
import {useSelector} from "react-redux";
import type {RootState} from "../store";
import {Canvas} from "@react-three/fiber";
import {Bounds, Environment, OrbitControls, useGLTF} from "@react-three/drei";
import {useGetPlanetSceneQuery} from "../store/api/sceneApi";

const PlanetTerrainModel: React.FC<{ url: string }> = ({url}) => {
    const gltf = useGLTF(url);
    return <primitive object={gltf.scene}/>;
};

export const PlanetScene: React.FC = () => {
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentSceneTerrainOptions);
    const {data} = useGetPlanetSceneQuery(currentSceneTerrainOption?.resolution ?? "R_9", {
        skip: !currentSceneTerrainOption?.resolution
    });
    const terrainUrl = data?.terrainPath ? `http://localhost:9000/terrains/${data.terrainPath}` : null;

    return (
        <Canvas
            shadows
            gl={{antialias: true}}
            camera={{fov: 35, near: 0.01, far: 10000, position: [3, 2, 3]}}
            style={{background: "#f3f4f6"}}
        >
            <OrbitControls
                makeDefault
                autoRotate={false}
                enableDamping
                dampingFactor={0.08}
                minDistance={0.1}
                maxDistance={5000}
                screenSpacePanning
            />
            <ambientLight intensity={0.35}/>
            <directionalLight position={[5, 10, 8]} intensity={1.1}/>
            <Environment preset="city"/>
            <Suspense fallback={null}>
                {terrainUrl && (
                    <Bounds fit clip observe margin={1.2}>
                        <PlanetTerrainModel url={terrainUrl}/>
                    </Bounds>
                )}
            </Suspense>
        </Canvas>
    );
};
