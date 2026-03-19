package io.lombardio.onlineauction.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OnlineAuctionAuthenticationFilter extends OncePerRequestFilter {

    private final IdentityAccessClient identityAccessClient;

    public OnlineAuctionAuthenticationFilter(IdentityAccessClient identityAccessClient) {
        this.identityAccessClient = identityAccessClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            identityAccessClient.fetchCurrentUser(authorization).ifPresent(user -> {
                var principal = new AuthenticatedOnlineAuctionUser(
                        user.userId(), user.tenantId(), user.email(), user.displayName(), user.permissions(), user.platformManager(), authorization
                );
                SecurityContextHolder.getContext().setAuthentication(principal);
            });
        }

        filterChain.doFilter(request, response);
    }
}
