package io.lombardio.aml.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AmlAuthenticationFilter extends OncePerRequestFilter {

    private final IdentityAccessClient identityAccessClient;

    public AmlAuthenticationFilter(IdentityAccessClient identityAccessClient) {
        this.identityAccessClient = identityAccessClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String tokenValue = authorization.substring(7);
            identityAccessClient.currentUser(tokenValue).ifPresent(currentUser -> {
                AuthenticatedAmlUser principal = new AuthenticatedAmlUser(
                        currentUser.id(),
                        currentUser.actorUserId(),
                        currentUser.tenantId(),
                        currentUser.impersonating(),
                        currentUser.permissions()
                );
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, tokenValue, List.of())
                );
            });
        }

        filterChain.doFilter(request, response);
    }
}
