package io.lombardio.aml.api.http.error;

public record ApiFieldError(
        String field,
        String message
) {
}
