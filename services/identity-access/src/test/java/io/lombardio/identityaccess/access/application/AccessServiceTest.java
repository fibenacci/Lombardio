package io.lombardio.identityaccess.access.application;

import io.lombardio.identityaccess.access.api.CreateUserRequest;
import io.lombardio.identityaccess.access.api.UpdateUserRequest;
import io.lombardio.identityaccess.access.domain.Branch;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.bootstrap.SeedFixtures;
import io.lombardio.identityaccess.support.InMemoryRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessServiceTest {

    private final InMemoryRepositories.Users users = new InMemoryRepositories.Users();
    private final InMemoryRepositories.Roles roles = new InMemoryRepositories.Roles();
    private final InMemoryRepositories.Branches branches = new InMemoryRepositories.Branches();
    private final InMemoryRepositories.Permissions permissions = new InMemoryRepositories.Permissions();
    private final InMemoryRepositories.Sessions sessions = new InMemoryRepositories.Sessions();
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-18T10:15:30Z"), ZoneOffset.UTC);

    private AccessService accessService;

    @BeforeEach
    void setUp() {
        SeedFixtures.PERMISSIONS.forEach(permissions::save);
        roles.save(SeedFixtures.ADMIN_ROLE);
        roles.save(SeedFixtures.REVIEW_ROLE);
        branches.save(SeedFixtures.defaultBranch());
        users.save(SeedFixtures.adminUser(passwordEncoder.encode("change-me")));

        accessService = new AccessService(users, roles, branches, permissions, sessions, passwordEncoder, clock);
    }

    @Test
    void shouldCreateUserWithEncodedPassword() {
        var created = accessService.createUser(new CreateUserRequest(
                SeedFixtures.DEFAULT_TENANT_ID,
                List.of(),
                "ops",
                "ops@lombardio.local",
                "TempPass123!",
                "Operations",
                "ACTIVE",
                List.of(SeedFixtures.REVIEW_ROLE.id())
        ));

        assertEquals("ops", created.username());
        assertEquals(2, users.findAll().size());
        var stored = users.findByEmail("ops@lombardio.local").orElseThrow();
        assertTrue(passwordEncoder.matches("TempPass123!", stored.passwordHash()));
    }

    @Test
    void shouldRejectUnknownRoleDuringUserCreation() {
        assertThrows(IllegalArgumentException.class, () -> accessService.createUser(new CreateUserRequest(
                SeedFixtures.DEFAULT_TENANT_ID,
                List.of(),
                "ops",
                "ops@lombardio.local",
                "TempPass123!",
                "Operations",
                "ACTIVE",
                List.of("role-missing")
        )));
    }

    @Test
    void shouldRejectRoleFromDifferentTenantDuringUserCreation() {
        roles.save(new Role(
                "role-other-tenant",
                "tenant-other",
                "tenant-other-admin",
                "Tenant Other Admin",
                "Role for another tenant",
                true,
                List.of("users.read")
        ));

        assertThrows(IllegalArgumentException.class, () -> accessService.createUserForTenant(
                SeedFixtures.DEFAULT_TENANT_ID,
                new CreateUserRequest(
                        null,
                        List.of(),
                        "ops",
                        "ops@lombardio.local",
                        "TempPass123!",
                        "Operations",
                        "ACTIVE",
                        List.of("role-other-tenant")
                )
        ));
    }

    @Test
    void shouldUseTenantPathScopeForUserCreation() {
        roles.save(new Role(
                "role-branch-tenant-review",
                "tenant-branch-1",
                "review",
                "Review",
                "Review role for branch tenant",
                true,
                List.of("users.read")
        ));

        var created = accessService.createUserForTenant("tenant-branch-1", new CreateUserRequest(
                null,
                List.of(),
                "ops",
                "ops@branch-1.lombardio.local",
                "TempPass123!",
                "Operations",
                "ACTIVE",
                List.of("role-branch-tenant-review")
        ));

        assertEquals("tenant-branch-1", created.tenantId());
    }

    @Test
    void shouldCreateBranchForTenant() {
        var created = accessService.createBranchForTenant(SeedFixtures.DEFAULT_TENANT_ID, new io.lombardio.identityaccess.access.api.CreateBranchRequest(
                "berlin-mitte",
                "Berlin Mitte",
                "ACTIVE"
        ));

        assertEquals(SeedFixtures.DEFAULT_TENANT_ID, created.tenantId());
        assertEquals("Berlin Mitte", created.displayName());
        assertEquals(2, branches.findAll().size());
    }

    @Test
    void shouldUpdateExistingUser() {
        var existing = users.findByEmail("admin@lombardio.local").orElseThrow();

        var updated = accessService.updateUser(existing.id(), new UpdateUserRequest(
                existing.tenantId(),
                List.of(SeedFixtures.DEFAULT_BRANCH_ID),
                existing.username(),
                existing.email(),
                "System Administrator",
                "ACTIVE",
                List.of(SeedFixtures.ADMIN_ROLE.id())
        ));

        assertEquals("System Administrator", updated.displayName());
        assertEquals(List.of(SeedFixtures.DEFAULT_BRANCH_ID), updated.branchIds());
        assertEquals("System Administrator", users.findById(existing.id()).orElseThrow().displayName());
    }

    @Test
    void shouldRejectBranchFromDifferentTenantDuringUserUpdate() {
        branches.save(new Branch(
                "branch-other-tenant",
                "tenant-other",
                "other",
                "Other Branch",
                "ACTIVE",
                Instant.parse("2026-03-18T10:15:30Z"),
                Instant.parse("2026-03-18T10:15:30Z")
        ));
        var existing = users.findByEmail("admin@lombardio.local").orElseThrow();

        assertThrows(IllegalArgumentException.class, () -> accessService.updateUser(existing.id(), new UpdateUserRequest(
                existing.tenantId(),
                List.of("branch-other-tenant"),
                existing.username(),
                existing.email(),
                existing.displayName(),
                existing.status(),
                existing.roleIds()
        )));
    }
}
