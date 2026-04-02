import { readRuntimeValue } from "../config/runtime-config";
import { useAuthStore } from "../../../app/session/state/auth.store";

const platformBaseUrl = readRuntimeValue(
  "PLATFORM_API_BASE_URL",
  import.meta.env.VITE_PLATFORM_API_BASE_URL ?? "http://localhost:8080"
);

export const BASE_URLS = {
  default: platformBaseUrl,
  platform: platformBaseUrl,
  customerPortal: readRuntimeValue(
    "CUSTOMER_API_BASE_URL",
    import.meta.env.VITE_CUSTOMER_API_BASE_URL ?? "http://localhost:8080"
  ),
  publicOnlineAuction: readRuntimeValue(
    "ONLINE_AUCTION_API_BASE_URL",
    import.meta.env.VITE_ONLINE_AUCTION_API_BASE_URL ?? "http://localhost:8080"
  )
};

function buildUserMessage(response, payload) {
  const hasFieldErrors =
    typeof payload === "object" && payload !== null && Array.isArray(payload.fieldErrors) && payload.fieldErrors.length > 0;

  if (response.status === 400 || response.status === 422 || hasFieldErrors) {
    return "Validation failed";
  }

  if (response.status === 401) {
    return "Authentication is required.";
  }

  if (response.status === 403) {
    return "You are not allowed to perform this action.";
  }

  if (response.status === 404) {
    return "The requested resource was not found.";
  }

  if (response.status === 409) {
    return "The request could not be completed.";
  }

  if (response.status === 502 || response.status === 503 || response.status === 504) {
    return "The service is temporarily unavailable.";
  }

  return "The request failed.";
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
  const message = buildUserMessage(response, payload);

  const error = new Error(message);
  error.userMessage = message;
  error.status = response.status;
  error.payload = payload;
  error.fieldErrors = fieldErrors;
  error.traceId = traceId;
  error.requestMethod = requestInfo.method;
  error.requestUrl = requestInfo.url;
  return error;
}

async function request(baseUrl, path, options = {}) {
  const authStore = useAuthStore();
  const { headers: customHeaders = {}, ...restOptions } = options;
  const requestUrl = `${baseUrl}${path}`;
  const requestMethod = restOptions.method ?? "GET";

  const response = await fetch(requestUrl, {
    ...restOptions,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
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

async function requestBlob(baseUrl, path) {
  const authStore = useAuthStore();
  const requestUrl = `${baseUrl}${path}`;
  const response = await fetch(requestUrl, {
    method: "GET",
    credentials: "include"
  });

  if (!response.ok) {
    const payload = await parsePayload(response);
    const error = toRequestError(response, payload, {
      method: "GET",
      url: requestUrl
    });

    if (response.status === 401) {
      await redirectToLogin(authStore);
    }

    throw error;
  }

  return response.blob();
}

function withJsonBody(method, body) {
  return {
    method,
    body: JSON.stringify(body)
  };
}

export function createApiClient(baseUrl) {
  return {
    get(path) {
      return request(baseUrl, path, { method: "GET" });
    },
    post(path, body) {
      return request(baseUrl, path, withJsonBody("POST", body));
    },
    patch(path, body) {
      return request(baseUrl, path, withJsonBody("PATCH", body));
    },
    put(path, body) {
      return request(baseUrl, path, withJsonBody("PUT", body));
    },
    getBlob(path) {
      return requestBlob(baseUrl, path);
    }
  };
}

export const apiClient = createApiClient(BASE_URLS.default);
