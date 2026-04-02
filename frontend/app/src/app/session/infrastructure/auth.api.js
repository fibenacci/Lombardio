import { readRuntimeValue } from "../../../shared/kernel/config/runtime-config";
import { apiClient, BASE_URLS } from "../../../shared/kernel/http/runtime-api-client";

const OPERATOR_DELEGATION_ENABLED = readRuntimeValue(
  "OPERATOR_DELEGATION_ENABLED",
  import.meta.env.VITE_OPERATOR_DELEGATION_ENABLED ?? "false"
) === "true";
const OPERATOR_TOTP_ENABLED = readRuntimeValue(
  "OPERATOR_TOTP_ENABLED",
  import.meta.env.VITE_OPERATOR_TOTP_ENABLED ?? "false"
) === "true";

const PLATFORM_AUTH_BASE_URL = readRuntimeValue(
  "PLATFORM_API_BASE_URL",
  import.meta.env.VITE_PLATFORM_API_BASE_URL ?? "http://localhost:8080"
);

async function parsePayload(response) {
  const isJson = response.headers.get("content-type")?.includes("application/json");
  return isJson ? await response.json() : await response.text();
}

function toRequestError(response, payload, fallbackMessage) {
  const message = response.status === 401 ? fallbackMessage : "The request failed.";

  const error = new Error(message);
  error.userMessage = message;
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
    credentials: "include"
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
  const response = await fetch(`${PLATFORM_AUTH_BASE_URL}/api/v1/platform/auth/logout`, {
    method: "POST",
    credentials: "include"
  });

  if (response.status === 204 || response.status === 401) {
    return;
  }

  const payload = await parsePayload(response);
  if (!response.ok) {
    throw toRequestError(response, payload, "Logout failed");
  }
}

export async function fetchCurrentUser() {
  const response = await fetch(`${BASE_URLS.platform}/api/v1/platform/auth/me`, {
    method: "GET",
    credentials: "include"
  });
  const payload = await parsePayload(response);
  if (!response.ok) {
    throw toRequestError(response, payload, "Current user lookup failed");
  }
  return payload;
}

export function isDelegationEnabled() {
  return OPERATOR_DELEGATION_ENABLED;
}

export function isTotpEnabled() {
  return OPERATOR_TOTP_ENABLED;
}

export function createDelegation(userId) {
  if (!OPERATOR_DELEGATION_ENABLED) {
    const error = new Error("Delegated sessions are not available.");
    error.userMessage = "Delegated sessions are not available.";
    throw error;
  }
  return apiClient.post("/api/v1/auth/delegations", { userId });
}

export function verifyTotpChallenge(payload) {
  if (!OPERATOR_TOTP_ENABLED) {
    const error = new Error("Two-factor authentication is not available.");
    error.userMessage = "Two-factor authentication is not available.";
    throw error;
  }
  return apiClient.post("/api/v1/auth/mfa/totp/verify", payload);
}

export function startTotpEnrollment() {
  if (!OPERATOR_TOTP_ENABLED) {
    const error = new Error("Two-factor authentication is not available.");
    error.userMessage = "Two-factor authentication is not available.";
    throw error;
  }
  return apiClient.post("/api/v1/auth/mfa/totp/enroll", {});
}

export function activateTotp(payload) {
  if (!OPERATOR_TOTP_ENABLED) {
    const error = new Error("Two-factor authentication is not available.");
    error.userMessage = "Two-factor authentication is not available.";
    throw error;
  }
  return apiClient.post("/api/v1/auth/mfa/totp/activate", payload);
}
