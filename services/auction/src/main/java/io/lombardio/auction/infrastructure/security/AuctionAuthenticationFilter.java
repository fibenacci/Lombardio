package io.lombardio.auction.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;

@Component
public class AuctionAuthenticationFilter extends OncePerRequestFilter {

    private final IdentityAccessClient identityAccessClient;

    public AuctionAuthenticationFilter(IdentityAccessClient identityAccessClient) {
        this.identityAccessClient = identityAccessClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        IdentityCurrentUser currentUser = identityAccessClient.fetchCurrentUser(header.substring("Bearer ".length()));
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuctionUser(
                currentUser.userId(),
                currentUser.tenantId(),
                currentUser.email(),
                currentUser.displayName(),
                new HashSet<>(currentUser.permissions())
        ));
        filterChain.doFilter(request, response);
    }
}
