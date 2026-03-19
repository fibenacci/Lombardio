package io.lombardio.auction.infrastructure.security;

public interface IdentityAccessClient {

    IdentityCurrentUser fetchCurrentUser(String token);
}
