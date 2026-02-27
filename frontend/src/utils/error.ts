/**
 * Извлекает читаемое сообщение из ошибки (RTK Query / API или Error).
 */
export function getErrorMessage(err: unknown, fallback = "Something went wrong"): string {
  if (err && typeof err === "object" && "data" in err) {
    const data = (err as { data?: unknown }).data;
    if (typeof data === "string") {
      return data;
    }
    if (
      data &&
      typeof data === "object" &&
      "message" in data &&
      typeof (data as { message?: unknown }).message === "string"
    ) {
      return (data as { message: string }).message;
    }
  } else if (err instanceof Error) {
    return err.message;
  }

  return fallback;
}
