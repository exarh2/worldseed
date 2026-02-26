import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export interface HelloResponse {
  message: string;
}

export interface SignUpRequest {
  login: string;
  password: string;
  email: string;
}

export interface SignInRequest {
  login: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  login: string;
  role: string;
}

export const baseApi = createApi({
  reducerPath: "baseApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api/",
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as { auth?: { token: string | null } }).auth
        ?.token;
      if (token) {
        headers.set("Authorization", `Bearer ${token}`);
      }
      return headers;
    }
  }),
  endpoints: (builder) => ({
    getHello: builder.query<HelloResponse, void>({
      query: () => "hello"
    }),
    signUp: builder.mutation<AuthResponse, SignUpRequest>({
      query: (body) => ({
        url: "v1/auth/sign-up",
        method: "POST",
        body
      })
    }),
    signIn: builder.mutation<AuthResponse, SignInRequest>({
      query: (body) => ({
        url: "v1/auth/sign-in",
        method: "POST",
        body
      })
    })
  })
});

export const { useGetHelloQuery, useSignUpMutation, useSignInMutation } =
  baseApi;

