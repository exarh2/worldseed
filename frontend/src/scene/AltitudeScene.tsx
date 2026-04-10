import React, {Suspense} from "react";
import {Canvas} from "@react-three/fiber";
import {Environment, OrbitControls, Stars, useGLTF} from "@react-three/drei";
import {useSelector} from "react-redux";
import type {RootState} from "../store";
import {useGetSceneStateQuery} from "../store/api/sceneApi";
import {config} from "../config";

const AltitudeTerrainModel: React.FC<{ url: string }> = ({url}) => {
    const gltf = useGLTF(url);
    return <primitive object={gltf.scene}/>;
};

export const AltitudeScene: React.FC = () => {
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentTerrainOptions);
    const mapView = useSelector((state: RootState) => state.ui.mapView);

    const {data} = useGetSceneStateQuery(
        {
            resolution: currentSceneTerrainOption!.resolution,
            longitude: mapView.center[0],
            latitude: mapView.center[1],
            terrainViewDistance:
                "maxTerrainViewDistance" in currentSceneTerrainOption!
                    ? currentSceneTerrainOption!.maxTerrainViewDistance
                    : 3
        }
    );
    const terrainUrls = (data?.terrainPaths ?? []).map((terrainPath) => `${config.terrainsBaseUrl}/${terrainPath}`);

    return (
        <Canvas
            shadows
            gl={{antialias: true}}
            camera={{fov: 35, near: 10, far: 135504085, position: [25504085, 0, 0], up: [0, 0, 1]}}
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
                makeDefault
                autoRotate={false}
                enableDamping
                dampingFactor={0.08}
                minDistance={6378137}
                maxDistance={50000000}
                screenSpacePanning
                target={[0, 0, 0]}
            />
            <ambientLight intensity={0.35}/>
            <directionalLight position={[5, 10, 8]} intensity={1.1}/>
            <Environment preset="city"/>
            <Suspense fallback={null}>
                {terrainUrls.map((terrainUrl) => (
                    <AltitudeTerrainModel key={terrainUrl} url={terrainUrl}/>
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
