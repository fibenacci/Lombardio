package io.lombardio.identityaccess.access.domain;

import java.util.List;

public record Role(
        String id,
        String tenantId,
        String key,
        String displayName,
        String description,
        boolean active,
        List<String> permissionKeys
) {
}
