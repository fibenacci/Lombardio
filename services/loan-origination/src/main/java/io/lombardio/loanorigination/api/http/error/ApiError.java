package io.lombardio.loanorigination.api.http.error;

import java.util.List;

public record ApiError(
        String code,
        String message,
        List<ApiFieldError> fieldErrors
) {
}
