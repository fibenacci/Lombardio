import { BASE_URLS, createApiClient } from "../../../../shared/kernel/http/runtime-api-client";

const apiClient = createApiClient(BASE_URLS.customerPortal);

async function parsePayload(response) {
  if (response.status === 204) {
    return null;
  }

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

async function requestCustomerPortalSession(path, body, fallbackMessage) {
  const response = await fetch(`${BASE_URLS.customerPortal}${path}`, {
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

export function fetchPortalInvitation(token) {
  return apiClient.post("/api/v1/customer-portal/invitations/lookup", { token });
}

export function acceptPortalInvitation(payload) {
  return requestCustomerPortalSession(
    "/api/v1/customer-portal/invitations/accept",
    payload,
    "Invitation could not be accepted."
  );
}

export function loginCustomerPortal(payload) {
  return requestCustomerPortalSession("/api/v1/customer-portal/auth/login", payload, "Login failed");
}

export async function refreshCustomerPortalSession() {
  const response = await fetch(`${BASE_URLS.customerPortal}/api/v1/customer-portal/auth/refresh`, {
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

export function logoutCustomerPortal() {
  return fetch(`${BASE_URLS.customerPortal}/api/v1/customer-portal/auth/logout`, {
    method: "POST",
    credentials: "include"
  }).then(async (response) => {
    if (response.status === 204 || response.status === 401) {
      return null;
    }

    const payload = await parsePayload(response);
    if (!response.ok) {
      throw toRequestError(response, payload, "Logout failed");
    }

    return payload;
  });
}

export function fetchCustomerPortalMe() {
  return fetch(`${BASE_URLS.customerPortal}/api/v1/customer-portal/auth/me`, {
    method: "GET",
    credentials: "include"
  }).then(async (response) => {
    const payload = await parsePayload(response);
    if (!response.ok) {
      throw toRequestError(response, payload, "The request failed.");
    }
    return payload;
  });
}

export function fetchCustomerPortalPawnTickets() {
  return fetch(`${BASE_URLS.customerPortal}/api/v1/customer-portal/pawn-tickets`, {
    method: "GET",
    credentials: "include"
  }).then(async (response) => {
    const payload = await parsePayload(response);
    if (!response.ok) {
      throw toRequestError(response, payload, "The request failed.");
    }
    return payload;
  });
}

export async function fetchCustomerPortalDocument(ticketNumber) {
  const response = await fetch(
    `${BASE_URLS.customerPortal}/api/v1/customer-portal/pawn-tickets/${encodeURIComponent(ticketNumber)}/document`,
    {
      method: "GET",
      credentials: "include"
    }
  );

  if (!response.ok) {
    const payload = await parsePayload(response);
    throw toRequestError(response, payload, "The request failed.");
  }

  return response.blob();
}
