package io.lombardio.loanorigination.infrastructure.security;

import java.util.Optional;

public interface IdentityAccessClient {

    Optional<IdentityCurrentUser> currentUser(String token);
}
