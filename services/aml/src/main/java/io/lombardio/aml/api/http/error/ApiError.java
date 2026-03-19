package io.lombardio.aml.api.http.error;

import java.util.List;

public record ApiError(
        String code,
        String message,
        List<ApiFieldError> fieldErrors
) {
}
