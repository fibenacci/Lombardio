package io.lombardio.identityaccess.demo;

import io.lombardio.identityaccess.access.domain.BranchRepository;
import io.lombardio.identityaccess.access.domain.Branch;
import io.lombardio.identityaccess.access.domain.RoleRepository;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.UserRepository;
import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.bootstrap.SeedFixtures;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
class ScenarioDataSeeder {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoDataProperties demoDataProperties;

    ScenarioDataSeeder(
            UserRepository userRepository,
            BranchRepository branchRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            DemoDataProperties demoDataProperties
    ) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoDataProperties = demoDataProperties;
    }

    void seed() {
        Instant baseInstant = Instant.now().minusSeconds(90L * 24L * 60L * 60L);
        String passwordHash = passwordEncoder.encode("change-me");
        Map<String, String> roleIds = new HashMap<>();
        Map<String, String> branchIds = new HashMap<>();

        for (var role : SeedFixtures.platformRoles()) {
            Role persisted = upsertRole(roleRepository, role);
            roleIds.put(role.id(), persisted.id());
        }

        for (var user : SeedFixtures.platformUsers(passwordHash, baseInstant)) {
            upsertUser(userRepository, remapUser(user, roleIds, branchIds));
        }

        int tenantIndex = 0;
        for (var tenant : SeedFixtures.businessTenants(demoDataProperties.effectiveScale())) {
            for (var branch : SeedFixtures.branchesForTenant(tenant, baseInstant.plusSeconds(tenantIndex * 3_600L))) {
                Branch persisted = upsertBranch(branchRepository, branch);
                branchIds.put(branch.id(), persisted.id());
            }
            for (var role : SeedFixtures.rolesForTenant(tenant)) {
                Role persisted = upsertRole(roleRepository, role);
                roleIds.put(role.id(), persisted.id());
            }
            for (var user : SeedFixtures.usersForTenant(
                    tenant,
                    passwordHash,
                    SeedFixtures.usersPerTenant(demoDataProperties.effectiveScale()),
                    baseInstant.plusSeconds(tenantIndex * 7_200L)
            )) {
                upsertUser(userRepository, remapUser(user, roleIds, branchIds));
            }
            tenantIndex++;
        }
    }

    private Role upsertRole(RoleRepository repository, Role seedRole) {
        return repository.findByTenantIdAndKey(seedRole.tenantId(), seedRole.key())
                .map(existing -> repository.save(new Role(
                        existing.id(),
                        seedRole.tenantId(),
                        seedRole.key(),
                        seedRole.displayName(),
                        seedRole.description(),
                        seedRole.active(),
                        seedRole.permissionKeys()
                )))
                .orElseGet(() -> repository.save(seedRole));
    }

    private Branch upsertBranch(BranchRepository repository, Branch seedBranch) {
        return repository.findByTenantIdAndKey(seedBranch.tenantId(), seedBranch.key())
                .map(existing -> repository.save(new Branch(
                        existing.id(),
                        seedBranch.tenantId(),
                        seedBranch.key(),
                        seedBranch.displayName(),
                        seedBranch.status(),
                        existing.createdAt(),
                        seedBranch.updatedAt()
                )))
                .orElseGet(() -> repository.save(seedBranch));
    }

    private User upsertUser(UserRepository repository, User seedUser) {
        return repository.findByTenantIdAndEmail(seedUser.tenantId(), seedUser.email())
                .map(existing -> repository.save(new User(
                        existing.id(),
                        seedUser.tenantId(),
                        seedUser.branchIds(),
                        seedUser.username(),
                        seedUser.email(),
                        existing.passwordHash(),
                        seedUser.displayName(),
                        seedUser.status(),
                        seedUser.roleIds(),
                        existing.createdAt(),
                        seedUser.updatedAt()
                )))
                .orElseGet(() -> repository.save(seedUser));
    }

    private User remapUser(User seedUser, Map<String, String> roleIds, Map<String, String> branchIds) {
        return new User(
                seedUser.id(),
                seedUser.tenantId(),
                seedUser.branchIds().stream().map(branchId -> branchIds.getOrDefault(branchId, branchId)).toList(),
                seedUser.username(),
                seedUser.email(),
                seedUser.passwordHash(),
                seedUser.displayName(),
                seedUser.status(),
                seedUser.roleIds().stream().map(roleId -> roleIds.getOrDefault(roleId, roleId)).toList(),
                seedUser.createdAt(),
                seedUser.updatedAt()
        );
    }
}
