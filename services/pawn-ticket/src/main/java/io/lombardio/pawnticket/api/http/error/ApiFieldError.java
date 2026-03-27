package io.lombardio.pawnticket.api.http.error;

public record ApiFieldError(
        String field,
        String message
) {
}
