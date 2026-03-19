package io.lombardio.aml.infrastructure.security;

import java.util.Optional;

public interface IdentityAccessClient {

    Optional<IdentityCurrentUser> currentUser(String token);
}
