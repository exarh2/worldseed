import { baseApi } from "./baseApi";

export interface DropAllTerrainsResult {
  inProgress: boolean;
}

export const adminApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    dropAllTerrains: builder.mutation<DropAllTerrainsResult, void>({
      query: () => ({
        url: "v1/admin/drop-all-terrains",
        method: "POST",
      }),
    }),
  }),
  overrideExisting: false,
});

export const { useDropAllTerrainsMutation } = adminApi;

