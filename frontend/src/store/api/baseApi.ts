import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface HelloResponse {
  message: string;
}

export const baseApi = createApi({
  reducerPath: "baseApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api/"
  }),
  endpoints: (builder) => ({
    getHello: builder.query<HelloResponse, void>({
      query: () => "hello"
    })
  })
});

export const { useGetHelloQuery } = baseApi;

