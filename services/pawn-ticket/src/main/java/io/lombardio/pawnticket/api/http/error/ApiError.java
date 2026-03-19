package io.lombardio.pawnticket.api.http;

import java.util.List;

public record ApiError(
        String code,
        String message,
        List<ApiFieldError> fieldErrors
) {
}
