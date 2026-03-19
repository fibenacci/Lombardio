package io.lombardio.kyc.security;

import java.util.Optional;

public interface IdentityAccessClient {

    Optional<IdentityCurrentUser> currentUser(String token);
}
