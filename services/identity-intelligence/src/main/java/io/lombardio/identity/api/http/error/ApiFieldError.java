package io.lombardio.identity.api.http.error;

public record ApiFieldError(
        String field,
        String message
) {
}
