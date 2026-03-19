package io.lombardio.identityaccess.shared.api;

public record ApiError(
        String code,
        String message
) {
}
