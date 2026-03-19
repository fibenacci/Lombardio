package io.lombardio.customer.api.http.error;

public record ApiFieldError(
        String field,
        String message
) {
}
