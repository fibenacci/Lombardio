export type FieldError = {
  field: string;
  message: string;
};

export interface RequestError extends Error {
  status: number;
  userMessage: string;
  payload?: unknown;
  fieldErrors: FieldError[];
  traceId: string | null;
  requestMethod: string;
  requestUrl: string;
}

export const HttpStatus = {
  OK: 200,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
  GATEWAY_TIMEOUT: 504,
} as const;
