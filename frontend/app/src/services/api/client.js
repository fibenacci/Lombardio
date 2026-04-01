import { readRuntimeValue } from "../../config/runtime-config";
import { useAuthStore } from "../../stores/auth";

export const BASE_URLS = {
  default: readRuntimeValue("API_BASE_URL", import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"),
  platform: readRuntimeValue(
    "PLATFORM_API_BASE_URL",
    import.meta.env.VITE_PLATFORM_API_BASE_URL ?? "http://localhost:8080"
  ),
  origination: readRuntimeValue(
    "ORIGINATION_API_BASE_URL",
    import.meta.env.VITE_ORIGINATION_API_BASE_URL ?? "http://localhost:8080"
  ),
  customer: readRuntimeValue(
    "CUSTOMER_API_BASE_URL",
    import.meta.env.VITE_CUSTOMER_API_BASE_URL ?? "http://localhost:8080"
  ),
  pawnTicket: readRuntimeValue(
    "PAWN_TICKET_API_BASE_URL",
    import.meta.env.VITE_PAWN_TICKET_API_BASE_URL ?? "http://localhost:8080"
  ),
  kyc: readRuntimeValue("KYC_API_BASE_URL", import.meta.env.VITE_KYC_API_BASE_URL ?? "http://localhost:8080"),
  aml: readRuntimeValue("AML_API_BASE_URL", import.meta.env.VITE_AML_API_BASE_URL ?? "http://localhost:8080"),
  auction: readRuntimeValue(
    "AUCTION_API_BASE_URL",
    import.meta.env.VITE_AUCTION_API_BASE_URL ?? "http://localhost:8080"
  ),
  onlineAuction: readRuntimeValue(
    "ONLINE_AUCTION_API_BASE_URL",
    import.meta.env.VITE_ONLINE_AUCTION_API_BASE_URL ?? "http://localhost:8080"
  ),
  reporting: readRuntimeValue(
    "REPORTING_API_BASE_URL",
    import.meta.env.VITE_REPORTING_API_BASE_URL ?? "http://localhost:8080"
  )
};

function buildErrorMessage(response, payload, requestInfo) {
  const traceId =
    (typeof payload === "object" && payload !== null && "traceId" in payload ? payload.traceId : null)
    ?? requestInfo.traceId
    ?? null;

  if (typeof payload === "object" && payload !== null && "message" in payload && payload.message) {
    return traceId ? `${payload.message} (traceId: ${traceId})` : payload.message;
  }

  const requestLabel = `${requestInfo.method} ${requestInfo.url}`;
  return traceId
    ? `${requestLabel} failed with status ${response.status} (traceId: ${traceId})`
    : `${requestLabel} failed with status ${response.status}`;
}

function resolveToken(token) {
  const authStore = useAuthStore();
  return {
    authStore,
    token: token || authStore.accessToken
  };
}

async function redirectToLogin(authStore) {
  await authStore.logout();
  window.location.assign("/login");
}

async function parsePayload(response) {
  if (response.status === 204) {
    return null;
  }

  const isJson = response.headers.get("content-type")?.includes("application/json");
  return isJson ? await response.json() : await response.text();
}

function toRequestError(response, payload, requestInfo) {
  const fieldErrors =
    typeof payload === "object" && payload !== null && Array.isArray(payload.fieldErrors)
      ? payload.fieldErrors
      : [];
  const traceId =
    response.headers.get("X-Trace-Id")
    ?? (typeof payload === "object" && payload !== null && "traceId" in payload ? payload.traceId : null);
  const message = buildErrorMessage(response, payload, {
    method: requestInfo.method,
    url: requestInfo.url,
    traceId
  });

  const error = new Error(message);
  error.status = response.status;
  error.payload = payload;
  error.fieldErrors = fieldErrors;
  error.traceId = traceId;
  error.requestMethod = requestInfo.method;
  error.requestUrl = requestInfo.url;
  return error;
}

async function request(baseUrl, path, options = {}) {
  const { authStore, token } = resolveToken(options.token);
  const { headers: customHeaders = {}, ...restOptions } = options;
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

  const payload = await parsePayload(response);

  if (!response.ok) {
    const error = toRequestError(response, payload, {
      method: requestMethod,
      url: requestUrl
    });

    if (response.status === 401) {
      await redirectToLogin(authStore);
    }

    throw error;
  }

  return payload;
}

async function requestBlob(baseUrl, path, token) {
  const { authStore, token: effectiveToken } = resolveToken(token);
  const requestUrl = `${baseUrl}${path}`;
  const response = await fetch(requestUrl, {
    method: "GET",
    headers: {
      ...(effectiveToken ? { Authorization: `Bearer ${effectiveToken}` } : {})
    }
  });

  if (!response.ok) {
    const error = new Error(`GET ${requestUrl} failed with status ${response.status}`);
    error.status = response.status;

    if (response.status === 401) {
      await redirectToLogin(authStore);
    }

    throw error;
  }

  return response.blob();
}

function withJsonBody(method, body, token) {
  return {
    method,
    body: JSON.stringify(body),
    token
  };
}

export function createApiClient(baseUrl) {
  return {
    get(path, token) {
      return request(baseUrl, path, { method: "GET", token });
    },
    post(path, body, token) {
      return request(baseUrl, path, withJsonBody("POST", body, token));
    },
    patch(path, body, token) {
      return request(baseUrl, path, withJsonBody("PATCH", body, token));
    },
    put(path, body, token) {
      return request(baseUrl, path, withJsonBody("PUT", body, token));
    },
    getBlob(path, token) {
      return requestBlob(baseUrl, path, token);
    }
  };
}

export const apiClient = createApiClient(BASE_URLS.default);
