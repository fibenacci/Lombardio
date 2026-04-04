import { readRuntimeValue } from "../config/runtime-config";
import { errorInterceptor } from "./error-interceptor";
import { RequestErrorFactory } from "./request-error-factory";

const platformBaseUrl = readRuntimeValue(
  "PLATFORM_API_BASE_URL",
  (import.meta as any).env.VITE_PLATFORM_API_BASE_URL ?? "http://localhost:8080"
);

export const BASE_URLS = {
  default: platformBaseUrl,
  platform: platformBaseUrl,
  customerPortal: readRuntimeValue(
    "CUSTOMER_API_BASE_URL",
    (import.meta as any).env.VITE_CUSTOMER_API_BASE_URL ?? "http://localhost:8080"
  ),
  publicOnlineAuction: readRuntimeValue(
    "ONLINE_AUCTION_API_BASE_URL",
    (import.meta as any).env.VITE_ONLINE_AUCTION_API_BASE_URL ?? "http://localhost:8080"
  )
};

async function parsePayload(response: Response): Promise<any> {
  if (response.status === 204) {
    return null;
  }

  const isJson = response.headers.get("content-type")?.includes("application/json");
  return isJson ? await response.json() : await response.text();
}

async function request(baseUrl: string, path: string, options: RequestInit = {}): Promise<any> {
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

  if (!response.ok) {
    const error = await RequestErrorFactory.fromResponse(response, {
      method: requestMethod,
      url: requestUrl
    });

    await errorInterceptor.dispatch(error);
    throw error;
  }

  return parsePayload(response);
}

async function requestBlob(baseUrl: string, path: string): Promise<Blob> {
  const requestUrl = `${baseUrl}${path}`;
  const response = await fetch(requestUrl, {
    method: "GET",
    credentials: "include"
  });

  if (!response.ok) {
    const error = await RequestErrorFactory.fromResponse(response, {
      method: "GET",
      url: requestUrl
    });

    await errorInterceptor.dispatch(error);
    throw error;
  }

  return response.blob();
}

function withJsonBody(method: string, body: any): RequestInit {
  return {
    method,
    body: JSON.stringify(body)
  };
}

export function createApiClient(baseUrl: string) {
  return {
    get(path: string) {
      return request(baseUrl, path, { method: "GET" });
    },
    post(path: string, body: any) {
      return request(baseUrl, path, withJsonBody("POST", body));
    },
    patch(path: string, body: any) {
      return request(baseUrl, path, withJsonBody("PATCH", body));
    },
    put(path: string, body: any) {
      return request(baseUrl, path, withJsonBody("PUT", body));
    },
    getBlob(path: string) {
      return requestBlob(baseUrl, path);
    }
  };
}

export const apiClient = createApiClient(BASE_URLS.default);
