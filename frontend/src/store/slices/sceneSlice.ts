import { createSlice } from "@reduxjs/toolkit";

export interface SceneTerrainOptions {
  /** Настройки разрешения */
  resolution: "R_1_128" | "R_1_64" | "R_1_16" | "R_1_4" | "R_1" | "R_3" | "R_9";
  /** Вид террейна (зависит зависимости от разрешения) */
  generationType: "TERRAIN_PLANET" | "TERRAIN_ALTITUDE" | "TERRAIN_OSM";
  /** Шаг нарезки сетки по широте */
  latStep: number;
  /** Максимальная видимость вокруг пользователя в террейнах */
  maxTerrainViewDistance?: number;
}

export interface SceneState {
  sceneTerrainOptions: SceneTerrainOptions[];
  currentSceneTerrainOptions: SceneTerrainOptions | null;
}

const initialState: SceneState = {
  sceneTerrainOptions: [],
  currentSceneTerrainOptions: null
};

const sceneSlice = createSlice({
  name: "scene",
  initialState,
  reducers: {
    setSceneTerrainOptions(state, action: { payload: SceneTerrainOptions[] }) {
      state.sceneTerrainOptions = action.payload;
      if (!state.currentSceneTerrainOptions) {
        state.currentSceneTerrainOptions =
          action.payload.find((option) => option.resolution === "R_3") ?? action.payload[0] ?? null;
      }
    },
    setCurrentSceneTerrainOption(state, action: { payload: SceneTerrainOptions | null }) {
      state.currentSceneTerrainOptions = action.payload;
    }
  }
});

export const { setSceneTerrainOptions, setCurrentSceneTerrainOption } = sceneSlice.actions;
export const sceneReducer = sceneSlice.reducer;
