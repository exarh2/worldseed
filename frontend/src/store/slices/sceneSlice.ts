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
  /** Целевой уровень OSM zoom */
  zoomFrom: number;
}

export interface AltitudeTerrainOptions extends TerrainOptions {
  generationType: TerrainType.TERRAIN_ALTITUDE;
  /** Максимальная видимость вокруг пользователя в террейнах */
  maxTerrainViewDistance: number;
}

export interface OsmTerrainOptions extends TerrainOptions {
  generationType: TerrainType.TERRAIN_OSM;
  /** Максимальная видимость вокруг пользователя в террейнах */
  maxTerrainViewDistance: number;
}

export interface PlanetTerrainOptions extends TerrainOptions {
  generationType: TerrainType.TERRAIN_PLANET;
}

export interface SceneState {
  terrainOptions: AnyTerrainOptions[];
  currentTerrainOptions: AnyTerrainOptions | null;
}

export type AnyTerrainOptions =
  | AltitudeTerrainOptions
  | OsmTerrainOptions
  | PlanetTerrainOptions;

const initialState: SceneState = {
  terrainOptions: [],
  currentTerrainOptions: null
};

const sceneSlice = createSlice({
  name: "scene",
  initialState,
  reducers: {
    setTerrainOptions(state, action: { payload: AnyTerrainOptions[] }) {
      state.terrainOptions = action.payload;
      if (!state.currentTerrainOptions) {
        state.currentTerrainOptions =
          action.payload.find((option) => option.resolution === Resolution.R_3) ?? action.payload[0] ?? null;
      }
    },
    setCurrentTerrainOption(state, action: { payload: AnyTerrainOptions | null }) {
      state.currentTerrainOptions = action.payload;
    }
  }
});

export const { setTerrainOptions, setCurrentTerrainOption } = sceneSlice.actions;
export const sceneReducer = sceneSlice.reducer;
