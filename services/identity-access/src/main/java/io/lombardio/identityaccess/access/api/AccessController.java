package io.lombardio.identityaccess.access.api;

import io.lombardio.identityaccess.access.application.AccessService;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.auth.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/users")
    public List<UserSummaryResponse> listUsers() {
        return accessService.listUsers();
    }

    @GetMapping("/tenants/{tenantId}/users")
    public List<UserSummaryResponse> listUsersByTenant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String tenantId
    ) {
        requireTenantReadAccess(principal, tenantId, "users.read");
        return accessService.listUsersByTenant(tenantId);
    }

    @GetMapping("/tenants/{tenantId}/branches")
    public List<BranchResponse> listBranchesByTenant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String tenantId
    ) {
        requireTenantReadAccess(principal, tenantId, "branches.read");
        return accessService.listBranchesByTenant(tenantId);
    }

    @PostMapping("/tenants/{tenantId}/branches")
    public BranchResponse createBranchForTenant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String tenantId,
            @Valid @RequestBody CreateBranchRequest request
    ) {
        requireTenantWriteAccess(principal, tenantId, "branches.write");
        return accessService.createBranchForTenant(tenantId, request);
    }

    @PostMapping("/tenants/{tenantId}/users")
    public UserSummaryResponse createUserForTenant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String tenantId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        requireTenantWriteAccess(principal, tenantId, "users.write");
        return accessService.createUserForTenant(tenantId, request);
    }

    @PostMapping("/users")
    public UserSummaryResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return accessService.createUser(request);
    }

    @PatchMapping("/users/{id}")
    public UserSummaryResponse updateUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = accessService.requireUser(id);
        requireTenantWriteAccess(principal, user.tenantId(), "users.write");
        return accessService.updateUser(id, request);
    }

    @GetMapping("/roles")
    public List<RoleResponse> listRoles() {
        return accessService.listRoles();
    }

    @GetMapping("/tenants/{tenantId}/roles")
    public List<RoleResponse> listRolesByTenant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String tenantId
    ) {
        requireTenantReadAccess(principal, tenantId, "roles.read");
        return accessService.listRolesByTenant(tenantId);
    }

    @PostMapping("/tenants/{tenantId}/roles")
    public RoleResponse createRoleForTenant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String tenantId,
            @Valid @RequestBody CreateRoleRequest request
    ) {
        requireTenantWriteAccess(principal, tenantId, "roles.write");
        return accessService.createRoleForTenant(tenantId, request);
    }

    @PostMapping("/roles")
    public RoleResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        return accessService.createRole(request);
    }

    @PatchMapping("/roles/{id}")
    public RoleResponse updateRole(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        Role role = accessService.requireRole(id);
        requireTenantWriteAccess(principal, role.tenantId(), "roles.write");
        return accessService.updateRole(id, request);
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> listPermissions() {
        return accessService.listPermissions();
    }

    private void requireTenantReadAccess(AuthenticatedUserPrincipal principal, String tenantId, String permission) {
        requirePermission(principal, permission);
        if (!tenantId.equals(principal.tenantId()) && !hasPermission(principal, "platform.tenants.read")) {
            throw new AccessDeniedException("Tenant access denied");
        }
    }

    private void requireTenantWriteAccess(AuthenticatedUserPrincipal principal, String tenantId, String permission) {
        requirePermission(principal, permission);
        if (!tenantId.equals(principal.tenantId()) && !hasPermission(principal, "platform.tenants.write")) {
            throw new AccessDeniedException("Tenant write access denied");
        }
    }

    private void requirePermission(AuthenticatedUserPrincipal principal, String permission) {
        if (!hasPermission(principal, permission)) {
            throw new AccessDeniedException("Missing permission: " + permission);
        }
    }

    private boolean hasPermission(AuthenticatedUserPrincipal principal, String permission) {
        return principal.authorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("PERMISSION_" + permission));
    }
}
