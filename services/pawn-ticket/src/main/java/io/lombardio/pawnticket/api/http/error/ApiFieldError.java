package io.lombardio.pawnticket.api.http;

public record ApiFieldError(
        String field,
        String message
) {
}
