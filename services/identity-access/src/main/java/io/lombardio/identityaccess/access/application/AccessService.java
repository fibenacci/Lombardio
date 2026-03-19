package io.lombardio.identityaccess.access.application;

import io.lombardio.identityaccess.access.api.BranchResponse;
import io.lombardio.identityaccess.access.api.CreateBranchRequest;
import io.lombardio.identityaccess.access.api.CreateRoleRequest;
import io.lombardio.identityaccess.access.api.CreateUserRequest;
import io.lombardio.identityaccess.access.api.PermissionResponse;
import io.lombardio.identityaccess.access.api.RoleResponse;
import io.lombardio.identityaccess.access.api.UpdateRoleRequest;
import io.lombardio.identityaccess.access.api.UpdateUserRequest;
import io.lombardio.identityaccess.access.api.UserSummaryResponse;
import io.lombardio.identityaccess.access.domain.Branch;
import io.lombardio.identityaccess.access.domain.BranchRepository;
import io.lombardio.identityaccess.access.domain.Permission;
import io.lombardio.identityaccess.access.domain.PermissionRepository;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.RoleRepository;
import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.access.domain.UserRepository;
import io.lombardio.identityaccess.auth.domain.SessionTokenRepository;
import io.lombardio.identityaccess.bootstrap.SeedFixtures;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccessService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PermissionRepository permissionRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AccessService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BranchRepository branchRepository,
            PermissionRepository permissionRepository,
            SessionTokenRepository sessionTokenRepository,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.permissionRepository = permissionRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    public List<UserSummaryResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    public List<BranchResponse> listBranchesByTenant(String tenantId) {
        return branchRepository.findAll().stream()
                .filter(branch -> branch.tenantId().equals(tenantId))
                .map(this::toBranchResponse)
                .toList();
    }

    public BranchResponse createBranchForTenant(String tenantId, CreateBranchRequest request) {
        Instant now = Instant.now(clock);
        Branch branch = new Branch(
                "branch-" + UUID.randomUUID(),
                tenantId,
                request.key(),
                request.displayName(),
                request.status(),
                now,
                now
        );

        return toBranchResponse(branchRepository.save(branch));
    }

    public List<UserSummaryResponse> listUsersByTenant(String tenantId) {
        return userRepository.findAll().stream()
                .filter(user -> user.tenantId().equals(tenantId))
                .map(this::toUserResponse)
                .toList();
    }

    public UserSummaryResponse createUser(CreateUserRequest request) {
        return createUserForTenant(resolveTenantId(request.tenantId()), request);
    }

    public UserSummaryResponse createUserForTenant(String tenantId, CreateUserRequest request) {
        validateRoleIds(request.roleIds(), tenantId);
        validateBranchIds(request.branchIds(), tenantId);
        Instant now = Instant.now(clock);

        User user = new User(
                "user-" + UUID.randomUUID(),
                tenantId,
                copyBranchIds(request.branchIds()),
                request.username(),
                request.email(),
                passwordEncoder.encode(request.initialPassword()),
                request.displayName(),
                request.status(),
                List.copyOf(request.roleIds()),
                now,
                now
        );

        return toUserResponse(userRepository.save(user));
    }

    public UserSummaryResponse updateUser(String id, UpdateUserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        String tenantId = resolveTenantId(request.tenantId(), existing.tenantId());

        if (!existing.tenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant reassignment is not allowed");
        }

        validateRoleIds(request.roleIds(), tenantId);
        validateBranchIds(request.branchIds(), tenantId);

        User updated = new User(
                existing.id(),
                tenantId,
                copyBranchIds(request.branchIds()),
                request.username(),
                request.email(),
                existing.passwordHash(),
                request.displayName(),
                request.status(),
                List.copyOf(request.roleIds()),
                existing.createdAt(),
                Instant.now(clock)
        );

        if (!existing.status().equals(request.status()) || !existing.roleIds().equals(request.roleIds())) {
            sessionTokenRepository.deleteByUserId(existing.id());
        }

        return toUserResponse(userRepository.save(updated));
    }

    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream().map(this::toRoleResponse).toList();
    }

    public List<RoleResponse> listRolesByTenant(String tenantId) {
        return roleRepository.findAll().stream()
                .filter(role -> role.tenantId().equals(tenantId))
                .map(this::toRoleResponse)
                .toList();
    }

    public RoleResponse createRole(CreateRoleRequest request) {
        return createRoleForTenant(resolveTenantId(request.tenantId()), request);
    }

    public RoleResponse createRoleForTenant(String tenantId, CreateRoleRequest request) {
        validatePermissionKeys(request.permissionKeys());

        Role role = new Role(
                "role-" + UUID.randomUUID(),
                tenantId,
                request.key(),
                request.displayName(),
                request.description(),
                request.active(),
                List.copyOf(request.permissionKeys())
        );

        return toRoleResponse(roleRepository.save(role));
    }

    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        validatePermissionKeys(request.permissionKeys());
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));
        String tenantId = resolveTenantId(request.tenantId(), existing.tenantId());

        if (!existing.tenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant reassignment is not allowed");
        }

        Role updated = new Role(
                existing.id(),
                tenantId,
                request.key(),
                request.displayName(),
                request.description(),
                request.active(),
                List.copyOf(request.permissionKeys())
        );

        return toRoleResponse(roleRepository.save(updated));
    }

    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionResponse(permission.key(), permission.displayName(), permission.description()))
                .toList();
    }

    public List<String> roleKeysForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return user.roleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId)))
                .map(Role::key)
                .toList();
    }

    public List<String> permissionsForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return user.roleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId)))
                .flatMap(role -> role.permissionKeys().stream())
                .distinct()
                .toList();
    }

    public User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public Role requireRole(String roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
    }

    private void validateRoleIds(List<String> roleIds, String tenantId) {
        for (String roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
            if (!role.tenantId().equals(tenantId)) {
                throw new IllegalArgumentException("Role does not belong to tenant: " + roleId);
            }
        }
    }

    private void validatePermissionKeys(List<String> permissionKeys) {
        for (String permissionKey : permissionKeys) {
            permissionRepository.findByKey(permissionKey)
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionKey));
        }
    }

    private UserSummaryResponse toUserResponse(User user) {
        return new UserSummaryResponse(
                user.id(),
                user.tenantId(),
                user.branchIds(),
                user.username(),
                user.email(),
                user.displayName(),
                user.status(),
                user.roleIds(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.id(),
                role.tenantId(),
                role.key(),
                role.displayName(),
                role.description(),
                role.active(),
                role.permissionKeys()
        );
    }

    private BranchResponse toBranchResponse(Branch branch) {
        return new BranchResponse(
                branch.id(),
                branch.tenantId(),
                branch.key(),
                branch.displayName(),
                branch.status(),
                branch.createdAt(),
                branch.updatedAt()
        );
    }

    private String resolveTenantId(String tenantId) {
        return resolveTenantId(tenantId, SeedFixtures.DEFAULT_TENANT_ID);
    }

    private String resolveTenantId(String tenantId, String fallbackTenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return fallbackTenantId;
        }
        return tenantId;
    }

    private void validateBranchIds(List<String> branchIds, String tenantId) {
        for (String branchId : copyBranchIds(branchIds)) {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));
            if (!branch.tenantId().equals(tenantId)) {
                throw new IllegalArgumentException("Branch does not belong to tenant: " + branchId);
            }
        }
    }

    private List<String> copyBranchIds(List<String> branchIds) {
        if (branchIds == null) {
            return List.of();
        }
        return List.copyOf(branchIds);
    }
}
