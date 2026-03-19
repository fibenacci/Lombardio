package io.lombardio.platform.security;

import java.util.Optional;

public interface IdentityAccessClient {

    Optional<IdentityCurrentUser> currentUser(String token);
}
