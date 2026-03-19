package io.lombardio.platform.shared.api;

public record ApiError(
        String code,
        String message
) {
}
