package io.lombardio.identityaccess.access.domain;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    List<Role> findAll();

    Optional<Role> findById(String id);

    Optional<Role> findByKey(String key);

    Role save(Role role);
}
