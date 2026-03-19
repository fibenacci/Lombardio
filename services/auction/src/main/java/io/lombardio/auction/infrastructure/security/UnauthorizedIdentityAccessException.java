package io.lombardio.auction.infrastructure.security;

public class UnauthorizedIdentityAccessException extends RuntimeException {

    public UnauthorizedIdentityAccessException(String message) {
        super(message);
    }

    public UnauthorizedIdentityAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
