import React, { useMemo } from "react";
import { Select } from "@mantine/core";
import { useDispatch, useSelector } from "react-redux";
import type { RootState, AppDispatch } from "../store";
import { setCurrentSceneTerrainOption } from "../store/slices/sceneSlice";

export const SceneSelector: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const sceneTerrainOptions = useSelector((state: RootState) => state.scene.sceneTerrainOptions);
  const currentSceneTerrainOption = useSelector((state: RootState) => state.scene.currentSceneTerrainOptions);

  const data = useMemo(
    () =>
      sceneTerrainOptions.map((option) => ({
        value: option.resolution,
        label: `${option.resolution} - ${option.generationType}`
      })),
    [sceneTerrainOptions]
  );

  return (
    <Select
      label="Scene"
      placeholder="Select scene"
      data={data}
      value={currentSceneTerrainOption?.resolution ?? null}
      onChange={(value) => {
        const selectedOption = sceneTerrainOptions.find((option) => option.resolution === value) ?? null;
        dispatch(setCurrentSceneTerrainOption(selectedOption));
      }}
      disabled={sceneTerrainOptions.length === 0}
      searchable
      clearable={false}
    />
  );
};
