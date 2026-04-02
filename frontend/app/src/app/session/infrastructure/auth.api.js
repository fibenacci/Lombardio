import { readRuntimeValue } from "../../../shared/kernel/config/runtime-config";
import { apiClient, BASE_URLS } from "../../../shared/kernel/http/runtime-api-client";

const PLATFORM_AUTH_BASE_URL = readRuntimeValue(
  "PLATFORM_API_BASE_URL",
  import.meta.env.VITE_PLATFORM_API_BASE_URL ?? "http://localhost:8080"
);

async function parsePayload(response) {
  const isJson = response.headers.get("content-type")?.includes("application/json");
  return isJson ? await response.json() : await response.text();
}

function toRequestError(response, payload, fallbackMessage) {
  const message =
    typeof payload === "object" && payload !== null && "message" in payload && payload.message
      ? payload.message
      : fallbackMessage;

  const error = new Error(message);
  error.status = response.status;
  error.payload = payload;
  return error;
}

async function requestPlatformSession(path, body, fallbackMessage) {
  const response = await fetch(`${PLATFORM_AUTH_BASE_URL}${path}`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body ?? {})
  });

  const payload = await parsePayload(response);

  if (!response.ok) {
    throw toRequestError(response, payload, fallbackMessage);
  }

  return payload;
}

export async function login(payload) {
  return requestPlatformSession("/api/v1/platform/auth/login", payload, "Login failed");
}

export async function refreshSession() {
  const response = await fetch(`${PLATFORM_AUTH_BASE_URL}/api/v1/platform/auth/refresh`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: "{}"
  });

  if (response.status === 204 || response.status === 401) {
    return null;
  }

  const payload = await parsePayload(response);
  if (!response.ok) {
    throw toRequestError(response, payload, "Session refresh failed");
  }

  return payload;
}

export async function logout() {
  await requestPlatformSession("/api/v1/platform/auth/logout", {}, "Logout failed");
}

export async function fetchCurrentUser(token) {
  const response = await fetch(`${BASE_URLS.platform}/api/v1/platform/auth/me`, {
    method: "GET",
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    }
  });
  const payload = await parsePayload(response);
  if (!response.ok) {
    throw toRequestError(response, payload, "Current user lookup failed");
  }
  return payload;
}

export function createDelegation(userId, token) {
  return apiClient.post("/api/v1/auth/delegations", { userId }, token);
}

export function verifyTotpChallenge(payload) {
  return apiClient.post("/api/v1/auth/mfa/totp/verify", payload);
}

export function startTotpEnrollment(token) {
  return apiClient.post("/api/v1/auth/mfa/totp/enroll", {}, token);
}

export function activateTotp(payload, token) {
  return apiClient.post("/api/v1/auth/mfa/totp/activate", payload, token);
}
