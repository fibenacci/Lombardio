package io.lombardio.identityaccess.access.domain;

public record Permission(
        String key,
        String displayName,
        String description
) {
}
