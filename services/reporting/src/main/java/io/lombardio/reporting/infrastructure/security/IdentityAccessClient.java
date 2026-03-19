package io.lombardio.reporting.infrastructure.security;

import java.util.Optional;

public interface IdentityAccessClient {

    Optional<IdentityCurrentUser> currentUser(String token);
}
