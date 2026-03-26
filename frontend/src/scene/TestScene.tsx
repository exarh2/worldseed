import React from "react";
import {useSelector} from "react-redux";
import type {RootState} from "../store";
import {Canvas} from "@react-three/fiber";
import {Sky} from "@react-three/drei";

export const PlanetScene: React.FC = () => {
    const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentSceneTerrainOptions);

    return (
        <Canvas camera={{fov: 45}} shadows>
            <Sky sunPosition={[100, 20, 100]}/>
        </Canvas>
    );
};
