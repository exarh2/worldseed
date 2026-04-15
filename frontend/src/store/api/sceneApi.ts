import { baseApi } from "./baseApi";
import { setTerrainOptions, type AnyTerrainOptions } from "../slices/sceneSlice";

export interface SceneConfigResult {
  sceneTerrainOptions: AnyTerrainOptions[];
}

export interface GeodeticPosition {
  lon: number;
  lat: number;
}

export interface GeocentricPosition {
  x: number;
  y: number;
  z: number;
  alt: number;
}

export interface PlanetSceneResult {
  terrainPath: string;
}

export interface SceneStateRequest {
  resolution: AnyTerrainOptions["resolution"];
  longitude: number;
  latitude: number;
  terrainViewDistance: number;
}

export interface SceneStateResult {
  terrainPaths: string[];
  waitingRowKeys: string[];
}

export interface SceneGenerationRequest {
  waitingRowKeys: string[];
}

export const sceneApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getSceneConfig: builder.query<SceneConfigResult, void>({
      query: () => ({
        url: "v1/scene/config",
        method: "GET"
      }),
      async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
        const { data } = await queryFulfilled;
        dispatch(setTerrainOptions(data.sceneTerrainOptions ?? []));
      }
    }),
    getAltByPosition: builder.mutation<GeocentricPosition, GeodeticPosition>({
      query: (position) => ({
        url: "v1/scene/alt-by-position",
        method: "POST",
        body: position
      })
    }),
    getPlanetScene: builder.query<PlanetSceneResult, AnyTerrainOptions["resolution"]>({
      query: (resolution) => ({
        url: `v1/scene/planet/${resolution}`,
        method: "GET"
      })
    }),
    getSceneState: builder.query<SceneStateResult, SceneStateRequest>({
      query: (request) => ({
        url: "v1/scene",
        method: "POST",
        body: request
      })
    }),
    getSceneGeneration: builder.mutation<SceneStateResult, SceneGenerationRequest>({
      query: (request) => ({
        url: "v1/scene/generation",
        method: "POST",
        body: request
      })
    })
  }),
  overrideExisting: false
});

export const {
  useGetSceneConfigQuery,
  useGetAltByPositionMutation,
  useGetPlanetSceneQuery,
  useGetSceneStateQuery,
  useLazyGetSceneStateQuery,
  useGetSceneGenerationMutation
} = sceneApi;
