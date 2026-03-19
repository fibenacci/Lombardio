import { get, post } from "./client";

export function login(payload) {
  return post("/api/v1/auth/login", payload);
}

export function logout(token) {
  return post("/api/v1/auth/logout", {}, token);
}

export function fetchCurrentUser(token) {
  return get("/api/v1/auth/me", token);
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
