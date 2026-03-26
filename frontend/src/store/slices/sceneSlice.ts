import { createSlice } from "@reduxjs/toolkit";

export interface SceneTerrainOptions {
  resolution: string;
  generationType: string;
  latStep: number;
  relativeHeightFrom: number;
  maxTerrainViewDistance?: number;
}

export interface SceneState {
  sceneTerrainOptions: SceneTerrainOptions[];
}

const initialState: SceneState = {
  sceneTerrainOptions: []
};

const sceneSlice = createSlice({
  name: "scene",
  initialState,
  reducers: {
    setSceneTerrainOptions(state, action: { payload: SceneTerrainOptions[] }) {
      state.sceneTerrainOptions = action.payload;
    }
  }
});

export const { setSceneTerrainOptions } = sceneSlice.actions;
export const sceneReducer = sceneSlice.reducer;
