package io.lombardio.auction.infrastructure.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Set;

public class AuthenticatedAuctionUser extends AbstractAuthenticationToken {

    private final String userId;
    private final String tenantId;
    private final String email;
    private final String displayName;
    private final Set<String> permissions;

    public AuthenticatedAuctionUser(
            String userId,
            String tenantId,
            String email,
            String displayName,
            Set<String> permissions
    ) {
        super(java.util.List.of());
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.displayName = displayName;
        this.permissions = permissions;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public String userId() { return userId; }
    public String tenantId() { return tenantId; }
    public String email() { return email; }
    public String displayName() { return displayName; }
    public Set<String> permissions() { return permissions; }
}
