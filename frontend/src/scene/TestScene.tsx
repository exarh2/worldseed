import React from "react";
import {Canvas} from "@react-three/fiber";
import {OrbitControls, Sky} from "@react-three/drei";

export const TestScene: React.FC = () => {
    return (
        <Canvas camera={{fov: 45, position: [8, 6, 10]}} shadows>
            <Sky sunPosition={[-100, 200, 100]}/>
            <OrbitControls enableDamping dampingFactor={0.08}/>
            <ambientLight intensity={0.4}/>
            <directionalLight
                position={[10, 15, 8]}
                intensity={1.2}
                castShadow
                shadow-mapSize-width={2048}
                shadow-mapSize-height={2048}
            />

            <mesh rotation={[-Math.PI / 2, 0, 0]} receiveShadow>
                <planeGeometry args={[40, 40]}/>
                <meshStandardMaterial color="#6b7280"/>
            </mesh>

            <mesh position={[-3, 1, 0]} castShadow>
                <boxGeometry args={[1.5, 1.5, 1.5]}/>
                <meshStandardMaterial color="#ef4444"/>
            </mesh>

            <mesh position={[0, 1, 0]} castShadow>
                <sphereGeometry args={[1, 32, 32]}/>
                <meshStandardMaterial color="#3b82f6"/>
            </mesh>

            <mesh position={[3, 1, -1]} castShadow>
                <coneGeometry args={[1, 2, 32]}/>
                <meshStandardMaterial color="#22c55e"/>
            </mesh>

            <mesh position={[0, 2.2, -3]} castShadow>
                <torusGeometry args={[0.8, 0.3, 16, 100]}/>
                <meshStandardMaterial color="#f59e0b"/>
            </mesh>
        </Canvas>
    );
};
