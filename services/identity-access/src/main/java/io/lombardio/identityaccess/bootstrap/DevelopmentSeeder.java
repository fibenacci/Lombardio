package io.lombardio.identityaccess.bootstrap;

import io.lombardio.identityaccess.access.domain.BranchRepository;
import io.lombardio.identityaccess.access.domain.PermissionRepository;
import io.lombardio.identityaccess.access.domain.RoleRepository;
import io.lombardio.identityaccess.access.domain.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DevelopmentSeeder {

    @Bean
    ApplicationRunner seedDevelopmentData(
            UserRepository userRepository,
            BranchRepository branchRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            SeedFixtures.PERMISSIONS.forEach(permission -> permissionRepository.findByKey(permission.key())
                    .orElseGet(() -> permissionRepository.save(permission)));

            roleRepository.save(SeedFixtures.PLATFORM_ADMIN_ROLE);
            roleRepository.save(SeedFixtures.ADMIN_ROLE);
            roleRepository.save(SeedFixtures.REVIEW_ROLE);
            branchRepository.save(SeedFixtures.defaultBranch());

            userRepository.findById("user-platform-admin")
                    .orElseGet(() -> userRepository.save(SeedFixtures.platformAdminUser(passwordEncoder.encode("change-me"))));
            userRepository.findById("user-admin")
                    .orElseGet(() -> userRepository.save(SeedFixtures.adminUser(passwordEncoder.encode("change-me"))));
        };
    }
}
