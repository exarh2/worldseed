import { baseApi } from "./baseApi";

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

export const authApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
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
  }),
  overrideExisting: false
});

export const { useSignUpMutation, useSignInMutation } = authApi;

