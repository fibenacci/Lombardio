package io.lombardio.identityaccess.demo;

import io.lombardio.identityaccess.access.domain.PermissionRepository;
import io.lombardio.identityaccess.bootstrap.SeedFixtures;
import org.springframework.stereotype.Component;

@Component
class ReferenceDataSeeder {

    private final PermissionRepository permissionRepository;

    ReferenceDataSeeder(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    void seed() {
        SeedFixtures.PERMISSIONS.forEach(permission -> permissionRepository.findByKey(permission.key())
                .orElseGet(() -> permissionRepository.save(permission)));
    }
}
