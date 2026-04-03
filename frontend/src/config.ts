export type AppEnv = "development" | "production" | "test";

const rawAppEnv = import.meta.env.VITE_APP_ENV as string | undefined;
const rawLogLevel = import.meta.env.VITE_LOG_LEVEL as string | undefined;
const rawReduxLoggerEnabled = import.meta.env
  .VITE_REDUX_LOGGER_ENABLED as string | undefined;
const rawTerrainsBaseUrl = import.meta.env
  .VITE_TERRAINS_BASE_URL as string | undefined;

const resolvedAppEnv: AppEnv =
  (rawAppEnv as AppEnv | undefined) ??
  (import.meta.env.DEV ? "development" : "production");

const resolvedLogLevel =
  rawLogLevel ?? (import.meta.env.DEV ? "debug" : "info");

const resolvedReduxLoggerEnabled =
  rawReduxLoggerEnabled !== undefined
    ? rawReduxLoggerEnabled.toLowerCase() === "true"
    : import.meta.env.DEV;

const resolvedTerrainsBaseUrl =
  (rawTerrainsBaseUrl ?? "http://localhost:9000/terrains").replace(/\/$/, "");

export const config = {
  appEnv: resolvedAppEnv,
  logLevel: resolvedLogLevel,
  isDev: resolvedAppEnv === "development",
  isProd: resolvedAppEnv === "production",
  reduxLoggerEnabled: resolvedReduxLoggerEnabled,
  terrainsBaseUrl: resolvedTerrainsBaseUrl
};

