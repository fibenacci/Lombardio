package io.lombardio.identityaccess.access.api;

import java.util.List;

public record RoleResponse(
        String id,
        String tenantId,
        String key,
        String displayName,
        String description,
        boolean active,
        List<String> permissionKeys
) {
}
