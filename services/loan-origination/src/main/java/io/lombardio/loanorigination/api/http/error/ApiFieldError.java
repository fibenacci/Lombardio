package io.lombardio.loanorigination.api.http.error;

public record ApiFieldError(
        String field,
        String message
) {
}
