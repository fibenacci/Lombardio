package io.lombardio.identityaccess.access.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    User save(User user);
}
