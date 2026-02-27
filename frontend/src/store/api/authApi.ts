import { baseApi } from "./baseApi";

/**
 * Запрос на регистрацию нового пользователя.
 */
export interface SignUpRequest {
  /** Логин пользователя (1–50 символов). */
  login: string;
  /** Пароль пользователя (4–100 символов). */
  password: string;
  /** Адрес электронной почты (валидный e-mail, до 255 символов). */
  email: string;
}

/**
 * Запрос на вход в систему.
 */
export interface SignInRequest {
  /** Логин пользователя. */
  login: string;
  /** Пароль пользователя. */
  password: string;
}

/**
 * Ответ с данными аутентификации.
 */
export interface AuthResponse {
  /** JWT-токен для авторизации запросов. */
  token: string;
  /** Логин пользователя. */
  login: string;
  /** Роль пользователя в системе. */
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

