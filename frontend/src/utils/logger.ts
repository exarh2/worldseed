import { config } from "../config";

type LogLevel = "debug" | "info" | "warn" | "error";

const levelPriority: Record<LogLevel, number> = {
  debug: 10,
  info: 20,
  warn: 30,
  error: 40
};

function normalizeLevel(level: string | undefined): LogLevel {
  switch (level?.toLowerCase()) {
    case "debug":
      return "debug";
    case "warn":
      return "warn";
    case "error":
      return "error";
    case "info":
    default:
      return "info";
  }
}

const currentLevel = normalizeLevel(config.logLevel);

function shouldLog(level: LogLevel): boolean {
  return levelPriority[level] >= levelPriority[currentLevel];
}

function baseLog(level: LogLevel, ...args: unknown[]) {
  if (!shouldLog(level)) return;

  const prefix = `[${level.toUpperCase()}]`;

  switch (level) {
    case "debug":
      console.debug(prefix, ...args);
      break;
    case "info":
      console.info(prefix, ...args);
      break;
    case "warn":
      console.warn(prefix, ...args);
      break;
    case "error":
      console.error(prefix, ...args);
      break;
  }
}

export const logger = {
  debug: (...args: unknown[]) => baseLog("debug", ...args),
  info: (...args: unknown[]) => baseLog("info", ...args),
  warn: (...args: unknown[]) => baseLog("warn", ...args),
  error: (...args: unknown[]) => baseLog("error", ...args)
};

export function createLogger(namespace: string) {
  return {
    debug: (...args: unknown[]) => logger.debug(`[${namespace}]`, ...args),
    info: (...args: unknown[]) => logger.info(`[${namespace}]`, ...args),
    warn: (...args: unknown[]) => logger.warn(`[${namespace}]`, ...args),
    error: (...args: unknown[]) => logger.error(`[${namespace}]`, ...args)
  };
}
