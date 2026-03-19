package io.lombardio.onlineauction.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Set;

public class AuthenticatedOnlineAuctionUser extends AbstractAuthenticationToken {

    private final String userId;
    private final String tenantId;
    private final String email;
    private final String displayName;
    private final Set<String> permissions;
    private final boolean platformManager;
    private final String bearerToken;

    public AuthenticatedOnlineAuctionUser(String userId,
                                          String tenantId,
                                          String email,
                                          String displayName,
                                          Set<String> permissions,
                                          boolean platformManager,
                                          String bearerToken) {
        super(java.util.List.of());
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.displayName = displayName;
        this.permissions = permissions;
        this.platformManager = platformManager;
        this.bearerToken = bearerToken;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    public String userId() { return userId; }
    public String tenantId() { return tenantId; }
    public String email() { return email; }
    public String displayName() { return displayName; }
    public Set<String> permissions() { return permissions; }
    public boolean platformManager() { return platformManager; }
    public String bearerToken() { return bearerToken; }
}
