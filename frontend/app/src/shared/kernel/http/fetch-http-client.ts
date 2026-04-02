import type { HttpClient } from "./http-client";

async function request<T>(method: string, url: string, payload?: unknown): Promise<T> {
  const response = await fetch(url, {
    method,
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: payload === undefined ? undefined : JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function createFetchHttpClient(): HttpClient {
  return {
    delete: (url) => request("DELETE", url),
    get: (url) => request("GET", url),
    post: (url, payload) => request("POST", url, payload),
    put: (url, payload) => request("PUT", url, payload)
  };
}
