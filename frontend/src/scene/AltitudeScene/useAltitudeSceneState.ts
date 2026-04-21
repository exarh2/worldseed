import {useEffect, useRef, useState} from "react";
import {
    type SceneStateRequest,
    type SceneStateResult,
    useGetSceneGenerationMutation,
    useLazyGetSceneStateQuery
} from "../../store/api/sceneApi";

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

export const useAltitudeSceneState = (sceneRequest: SceneStateRequest | null): SceneStateResult | null => {
    const [sceneState, setSceneState] = useState<SceneStateResult | null>(null);
    const [triggerGetSceneState] = useLazyGetSceneStateQuery();
    const [getSceneGeneration] = useGetSceneGenerationMutation();
    const lastRenderedTerrainPathsRef = useRef<string[]>([]);

    useEffect(() => {
        if (!sceneRequest) {
            setSceneState(null);
            return;
        }

        let isCancelled = false;
        let lastGenerationRequestAt = 0;
        const loadScene = async () => {
            try {
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

    return sceneState;
};
