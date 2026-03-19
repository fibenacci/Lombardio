package io.lombardio.identityaccess.access.domain;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository {

    List<Permission> findAll();

    Optional<Permission> findByKey(String key);

    Permission save(Permission permission);
}
