import { HttpStatus, type FieldError, type RequestError } from "./types";

type ErrorPayload = {
  fieldErrors?: unknown;
  traceId?: unknown;
};

function isErrorPayload(payload: unknown): payload is ErrorPayload {
  return typeof payload === "object" && payload !== null;
}

function isFieldError(value: unknown): value is FieldError {
  return typeof value === "object"
    && value !== null
    && "field" in value
    && typeof value.field === "string"
    && "message" in value
    && typeof value.message === "string";
}

function buildUserMessage(response: Response, payload: unknown): string {
  const hasFieldErrors =
    isErrorPayload(payload)
    && Array.isArray(payload.fieldErrors)
    && payload.fieldErrors.some(isFieldError);

  if (response.status === HttpStatus.BAD_REQUEST || response.status === HttpStatus.UNPROCESSABLE_ENTITY || hasFieldErrors) {
    return "Validation failed";
  }

  const messages: Record<number, string> = {
    [HttpStatus.UNAUTHORIZED]: "Authentication is required.",
    [HttpStatus.FORBIDDEN]: "You are not allowed to perform this action.",
    [HttpStatus.NOT_FOUND]: "The requested resource was not found.",
    [HttpStatus.CONFLICT]: "The request could not be completed.",
    [HttpStatus.BAD_GATEWAY]: "The service is temporarily unavailable.",
    [HttpStatus.SERVICE_UNAVAILABLE]: "The service is temporarily unavailable.",
    [HttpStatus.GATEWAY_TIMEOUT]: "The service is temporarily unavailable.",
  };

  return messages[response.status] || "The request failed.";
}

async function parsePayload(response: Response): Promise<unknown> {
  if (response.status === HttpStatus.NO_CONTENT) {
    return null;
  }

  const isJson = response.headers.get("content-type")?.includes("application/json");
  return isJson ? await response.json() : await response.text();
}

export const RequestErrorFactory = {
  async fromResponse(response: Response, requestInfo: { method: string; url: string }): Promise<RequestError> {
    const payload = await parsePayload(response);
    const fieldErrors =
      isErrorPayload(payload) && Array.isArray(payload.fieldErrors)
        ? payload.fieldErrors.filter(isFieldError)
        : [];
    const traceId =
      response.headers.get("X-Trace-Id") ??
      (isErrorPayload(payload) && typeof payload.traceId === "string" ? payload.traceId : null);
    
    const message = buildUserMessage(response, payload);

    const error = new Error(message) as RequestError;
    error.userMessage = message;
    error.status = response.status;
    error.payload = payload;
    error.fieldErrors = fieldErrors;
    error.traceId = traceId;
    error.requestMethod = requestInfo.method;
    error.requestUrl = requestInfo.url;
    
    return error;
  }
};
