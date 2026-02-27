import { baseApi } from "./baseApi";

export interface HelloResponse {
  message: string;
}

export const helloApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getHello: builder.query<HelloResponse, void>({
      query: () => "hello"
    })
  }),
  overrideExisting: false
});

export const { useGetHelloQuery } = helloApi;

