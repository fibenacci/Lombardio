export function toQueryString(params: Record<string, string | number | boolean | null | undefined>) {
  const entries = Object.entries(params)
    .filter(([, value]) => value !== null && value !== undefined && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`);

  return entries.length ? `?${entries.join("&")}` : "";
}
