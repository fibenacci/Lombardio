package io.lombardio.identityaccess.access.api;

public record PermissionResponse(
        String key,
        String displayName,
        String description
) {
}
