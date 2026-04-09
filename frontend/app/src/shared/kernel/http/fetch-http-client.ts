import Cookies from "js-cookie";
import type { HttpClient } from "./http-client";

async function request<T>(method: string, url: string, payload?: unknown): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json"
  };

  // Add CSRF token for mutating requests
  if (["POST", "PUT", "PATCH", "DELETE"].includes(method.toUpperCase())) {
    const csrfToken = Cookies.get("XSRF-TOKEN");
    if (csrfToken) {
      headers["X-XSRF-TOKEN"] = csrfToken;
    }
  }

  const response = await fetch(url, {
    method,
    credentials: "include",
    headers,
    body: payload === undefined ? undefined : JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  // Handle empty responses (like 204 No Content)
  if (response.status === 204) {
    return {} as T;
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
