package io.lombardio.auction.api.http.error;

public record ApiFieldError(
        String field,
        String message
) {
}
