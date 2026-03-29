import { readRuntimeValue } from "../../config/runtime-config";
import { post } from "./client";

const KEYCLOAK_BASE_URL = readRuntimeValue(
  "KEYCLOAK_BASE_URL",
  import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8080"
);
const KEYCLOAK_REALM = readRuntimeValue(
  "KEYCLOAK_REALM",
  import.meta.env.VITE_KEYCLOAK_REALM ?? "lombardio"
);
const KEYCLOAK_CLIENT_ID = readRuntimeValue(
  "KEYCLOAK_CLIENT_ID",
  import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "lombardio-app"
);

function parseJwt(token) {
  const segments = token.split(".");
  if (segments.length < 2) {
    throw new Error("Invalid access token");
  }

  const payload = segments[1]
    .replace(/-/g, "+")
    .replace(/_/g, "/")
    .padEnd(Math.ceil(segments[1].length / 4) * 4, "=");

  return JSON.parse(window.atob(payload));
}

function toUserProfile(token) {
  const claims = parseJwt(token);
  const permissions = Array.isArray(claims.realm_access?.roles) ? claims.realm_access.roles : [];

  return {
    id: claims.sub,
    actorUserId: claims.actorUserId ?? claims.sub,
    tenantId: claims.tenantId ?? null,
    email: claims.email ?? claims.preferred_username ?? "",
    displayName: claims.name ?? claims.preferred_username ?? claims.email ?? "",
    impersonating: claims.impersonating === true,
    roles: permissions,
    permissions
  };
}

export async function login(payload) {
  const body = new URLSearchParams({
    grant_type: "password",
    client_id: KEYCLOAK_CLIENT_ID,
    username: payload.email,
    password: payload.password
  });

  const response = await fetch(
    `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body
    }
  );

  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payloadBody = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const errorDescription =
      typeof payloadBody === "object" && payloadBody !== null
        ? payloadBody.error_description || payloadBody.error
        : "";
    const message = errorDescription || "Login failed";
    const error = new Error(message);
    error.status = response.status;
    error.payload = payloadBody;
    throw error;
  }

  return {
    status: "AUTHENTICATED",
    accessToken: payloadBody.access_token
  };
}

export async function logout() {
  return null;
}

export async function fetchCurrentUser(token) {
  return toUserProfile(token);
}

export function createDelegation(userId, token) {
  return post("/api/v1/auth/delegations", { userId }, token);
}

export function verifyTotpChallenge(payload) {
  return post("/api/v1/auth/mfa/totp/verify", payload);
}

export function startTotpEnrollment(token) {
  return post("/api/v1/auth/mfa/totp/enroll", {}, token);
}

export function activateTotp(payload, token) {
  return post("/api/v1/auth/mfa/totp/activate", payload, token);
}
