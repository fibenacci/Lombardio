import type { HttpClient } from "./http-client";

async function request<T>(method: string, url: string, payload?: unknown, token?: string): Promise<T> {
  const response = await fetch(url, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
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
    delete: (url, token) => request("DELETE", url, undefined, token),
    get: (url, token) => request("GET", url, undefined, token),
    post: (url, payload, token) => request("POST", url, payload, token),
    put: (url, payload, token) => request("PUT", url, payload, token)
  };
}
