import { readRuntimeValue } from "../../config/runtime-config";
import { useAuthStore } from "../../stores/auth";

const API_BASE_URL = readRuntimeValue("API_BASE_URL", import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080");
const PLATFORM_API_BASE_URL = readRuntimeValue("PLATFORM_API_BASE_URL", import.meta.env.VITE_PLATFORM_API_BASE_URL ?? "http://localhost:8080");
const ORIGINATION_API_BASE_URL = readRuntimeValue("ORIGINATION_API_BASE_URL", import.meta.env.VITE_ORIGINATION_API_BASE_URL ?? "http://localhost:8080");
const CUSTOMER_API_BASE_URL = readRuntimeValue("CUSTOMER_API_BASE_URL", import.meta.env.VITE_CUSTOMER_API_BASE_URL ?? "http://localhost:8080");
const PAWN_TICKET_API_BASE_URL = readRuntimeValue("PAWN_TICKET_API_BASE_URL", import.meta.env.VITE_PAWN_TICKET_API_BASE_URL ?? "http://localhost:8080");
const KYC_API_BASE_URL = readRuntimeValue("KYC_API_BASE_URL", import.meta.env.VITE_KYC_API_BASE_URL ?? "http://localhost:8080");
const AML_API_BASE_URL = readRuntimeValue("AML_API_BASE_URL", import.meta.env.VITE_AML_API_BASE_URL ?? "http://localhost:8080");
const AUCTION_API_BASE_URL = readRuntimeValue("AUCTION_API_BASE_URL", import.meta.env.VITE_AUCTION_API_BASE_URL ?? "http://localhost:8080");
const ONLINE_AUCTION_API_BASE_URL = readRuntimeValue("ONLINE_AUCTION_API_BASE_URL", import.meta.env.VITE_ONLINE_AUCTION_API_BASE_URL ?? "http://localhost:8080");
const REPORTING_API_BASE_URL = readRuntimeValue("REPORTING_API_BASE_URL", import.meta.env.VITE_REPORTING_API_BASE_URL ?? "http://localhost:8080");

function buildErrorMessage(response, payload, requestInfo) {
  const traceId =
    (typeof payload === "object" && payload !== null && "traceId" in payload ? payload.traceId : null)
    ?? requestInfo.traceId
    ?? null;

  if (typeof payload === "object" && payload !== null && "message" in payload && payload.message) {
    return traceId
      ? `${payload.message} (traceId: ${traceId})`
      : payload.message;
  }

  const requestLabel = `${requestInfo.method} ${requestInfo.url}`;
  return traceId
    ? `${requestLabel} failed with status ${response.status} (traceId: ${traceId})`
    : `${requestLabel} failed with status ${response.status}`;
}

async function request(baseUrl, path, options = {}) {
  const authStore = useAuthStore();
  const { headers: customHeaders = {}, ...restOptions } = options;

  const token = options.token || authStore.accessToken;
  const requestUrl = `${baseUrl}${path}`;
  const requestMethod = restOptions.method ?? "GET";

  const response = await fetch(requestUrl, {
    ...restOptions,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...customHeaders
    }
  });

  if (response.status === 204) {
    return null;
  }

  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payload = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    if (response.status === 401) {
      // Trigger logout if we get an Unauthorized response
      authStore.logout();
      window.location.href = "/login";
    }

    const fieldErrors =
      typeof payload === "object" && payload !== null && Array.isArray(payload.fieldErrors)
        ? payload.fieldErrors
        : [];
    const traceId =
      response.headers.get("X-Trace-Id")
      ?? (typeof payload === "object" && payload !== null && "traceId" in payload ? payload.traceId : null);
    const message = buildErrorMessage(response, payload, {
      method: requestMethod,
      url: requestUrl,
      traceId
    });

    const error = new Error(message);
    error.status = response.status;
    error.payload = payload;
    error.fieldErrors = fieldErrors;
    error.traceId = traceId;
    error.requestMethod = requestMethod;
    error.requestUrl = requestUrl;
    throw error;
  }

  return payload;
}

export function get(path, token) {
  return request(API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function post(path, body, token) {
  return request(API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function patch(path, body, token) {
  return request(API_BASE_URL, path, {
    method: "PATCH",
    body: JSON.stringify(body),
    token
  });
}

export function put(path, body, token) {
  return request(API_BASE_URL, path, {
    method: "PUT",
    body: JSON.stringify(body),
    token
  });
}

export function platformGet(path, token) {
  return request(PLATFORM_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function platformPost(path, body, token) {
  return request(PLATFORM_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function platformPatch(path, body, token) {
  return request(PLATFORM_API_BASE_URL, path, {
    method: "PATCH",
    body: JSON.stringify(body),
    token
  });
}

export function platformPut(path, body, token) {
  return request(PLATFORM_API_BASE_URL, path, {
    method: "PUT",
    body: JSON.stringify(body),
    token
  });
}

export function originationGet(path, token) {
  return request(ORIGINATION_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function originationPost(path, body, token) {
  return request(ORIGINATION_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function customerGet(path, token) {
  return request(CUSTOMER_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export async function customerGetBlob(path, token) {
  const authStore = useAuthStore();
  const effectiveToken = token || authStore.accessToken;
  
  const response = await fetch(`${CUSTOMER_API_BASE_URL}${path}`, {
    method: "GET",
    headers: {
        ...(effectiveToken ? { Authorization: `Bearer ${effectiveToken}` } : {})
    }
  });

  if (!response.ok) {
    const error = new Error(`Request failed with status ${response.status}`);
    error.status = response.status;
    throw error;
  }

  return response.blob();
}

export function customerPost(path, body, token) {
  return request(CUSTOMER_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function customerPut(path, body, token) {
  return request(CUSTOMER_API_BASE_URL, path, {
    method: "PUT",
    body: JSON.stringify(body),
    token
  });
}

export function pawnTicketPost(path, body, token) {
  return request(PAWN_TICKET_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function pawnTicketGet(path, token) {
  return request(PAWN_TICKET_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function pawnTicketRootPost(path, body, token) {
  return request(PAWN_TICKET_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export async function pawnTicketGetBlob(path, token) {
  const authStore = useAuthStore();
  const effectiveToken = token || authStore.accessToken;

  const response = await fetch(`${PAWN_TICKET_API_BASE_URL}${path}`, {
    method: "GET",
    headers: {
        ...(effectiveToken ? { Authorization: `Bearer ${effectiveToken}` } : {})
    }
  });

  if (!response.ok) {
    const error = new Error(`Request failed with status ${response.status}`);
    error.status = response.status;
    throw error;
  }

  return response.blob();
}

export function kycGet(path, token) {
  return request(KYC_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function kycPost(path, body, token) {
  return request(KYC_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function amlGet(path, token) {
  return request(AML_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function amlPost(path, body, token) {
  return request(AML_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function auctionGet(path, token) {
  return request(AUCTION_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function auctionPost(path, body, token) {
  return request(AUCTION_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function onlineAuctionGet(path, token) {
  return request(ONLINE_AUCTION_API_BASE_URL, path, {
    method: "GET",
    token
  });
}

export function onlineAuctionPost(path, body, token) {
  return request(ONLINE_AUCTION_API_BASE_URL, path, {
    method: "POST",
    body: JSON.stringify(body),
    token
  });
}

export function reportingGet(path, token) {
  return request(REPORTING_API_BASE_URL, path, {
    method: "GET",
    token
  });
}
