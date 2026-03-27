package io.lombardio.platform.permission.api;

import io.lombardio.platform.iam.application.KeycloakService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform/permissions")
public class RolePermissionController {

    private final KeycloakService keycloakService;

    public RolePermissionController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('platform.tenants.read')")
    public List<String> listRoles() {
        return keycloakService.getAvailableRoles();
    }
}
