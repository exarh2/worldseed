import { baseApi } from "./baseApi";
import { setSceneTerrainOptions, type SceneTerrainOptions } from "../slices/sceneSlice";

export interface SceneConfigResult {
  sceneTerrainOptions: SceneTerrainOptions[];
}

export interface PlanetSceneResult {
  terrainPath: string;
}

export const sceneApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getSceneConfig: builder.query<SceneConfigResult, void>({
      query: () => ({
        url: "v1/scene/config",
        method: "POST",
        body: {}
      }),
      async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
        const { data } = await queryFulfilled;
        dispatch(setSceneTerrainOptions(data.sceneTerrainOptions ?? []));
      }
    }),
    getPlanetScene: builder.query<PlanetSceneResult, SceneTerrainOptions["resolution"]>({
      query: (resolution) => ({
        url: `v1/scene/planet/${resolution}`,
        method: "GET"
      })
    })
  }),
  overrideExisting: false
});

export const { useGetSceneConfigQuery, useGetPlanetSceneQuery } = sceneApi;
