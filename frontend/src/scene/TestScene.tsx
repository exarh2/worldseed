import React from "react";
import {Canvas} from "@react-three/fiber";
import {Sky} from "@react-three/drei";

export const TestScene: React.FC = () => {
    return (
        <Canvas camera={{fov: 45}} shadows>
            <Sky sunPosition={[-100, 200, 100]}/>
        </Canvas>
    );
};
