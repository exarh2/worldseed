export type AppEnv = "development" | "production" | "test";

const rawAppEnv = import.meta.env.VITE_APP_ENV as string | undefined;
const rawLogLevel = import.meta.env.VITE_LOG_LEVEL as string | undefined;

const resolvedAppEnv: AppEnv =
  (rawAppEnv as AppEnv | undefined) ??
  (import.meta.env.DEV ? "development" : "production");

const resolvedLogLevel =
  rawLogLevel ?? (import.meta.env.DEV ? "debug" : "info");

export const config = {
  appEnv: resolvedAppEnv,
  logLevel: resolvedLogLevel,
  isDev: resolvedAppEnv === "development",
  isProd: resolvedAppEnv === "production"
};

