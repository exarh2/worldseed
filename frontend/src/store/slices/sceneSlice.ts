import { createSlice } from "@reduxjs/toolkit";

export enum Resolution {
  R_1_128 = "R_1_128",
  R_1_64 = "R_1_64",
  R_1_16 = "R_1_16",
  R_1_4 = "R_1_4",
  R_1 = "R_1",
  R_3 = "R_3"
}

export enum TerrainType {
  TERRAIN_PLANET = "TERRAIN_PLANET",
  TERRAIN_ALTITUDE = "TERRAIN_ALTITUDE",
  TERRAIN_OSM = "TERRAIN_OSM"
}

export interface TerrainOptions {
  /** Настройки разрешения */
  resolution: Resolution;
  /** Вид террейна (зависит зависимости от разрешения) */
  generationType: TerrainType;
  /** Шаг нарезки сетки по широте */
  latStep: number;
}

export interface AltitudeSceneTerrainOptions extends TerrainOptions {
  generationType: TerrainType.TERRAIN_ALTITUDE;
  /** Максимальная видимость вокруг пользователя в террейнах */
  maxTerrainViewDistance: number;
}

export interface OsmSceneTerrainOptions extends TerrainOptions {
  generationType: TerrainType.TERRAIN_OSM;
  /** Максимальная видимость вокруг пользователя в террейнах */
  maxTerrainViewDistance: number;
}

export interface PlanetSceneTerrainOptions extends TerrainOptions {
  generationType: TerrainType.TERRAIN_PLANET;
}

export interface SceneState {
  sceneTerrainOptions: AnySceneTerrainOptions[];
  currentSceneTerrainOptions: AnySceneTerrainOptions | null;
}

export type AnySceneTerrainOptions =
  | AltitudeSceneTerrainOptions
  | OsmSceneTerrainOptions
  | PlanetSceneTerrainOptions;

const initialState: SceneState = {
  sceneTerrainOptions: [],
  currentSceneTerrainOptions: null
};

const sceneSlice = createSlice({
  name: "scene",
  initialState,
  reducers: {
    setSceneTerrainOptions(state, action: { payload: AnySceneTerrainOptions[] }) {
      state.sceneTerrainOptions = action.payload;
      if (!state.currentSceneTerrainOptions) {
        state.currentSceneTerrainOptions =
          action.payload.find((option) => option.resolution === Resolution.R_3) ?? action.payload[0] ?? null;
      }
    },
    setCurrentSceneTerrainOption(state, action: { payload: AnySceneTerrainOptions | null }) {
      state.currentSceneTerrainOptions = action.payload;
    }
  }
});

export const { setSceneTerrainOptions, setCurrentSceneTerrainOption } = sceneSlice.actions;
export const sceneReducer = sceneSlice.reducer;
