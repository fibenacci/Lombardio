package io.lombardio.onlineauction.security;

import java.util.Optional;

public interface IdentityAccessClient {
    Optional<IdentityCurrentUser> fetchCurrentUser(String bearerToken);
}
