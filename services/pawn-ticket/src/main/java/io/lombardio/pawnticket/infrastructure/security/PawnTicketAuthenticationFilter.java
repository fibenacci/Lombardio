package io.lombardio.pawnticket.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Component
public class PawnTicketAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PawnTicketAuthenticationFilter.class);
    private final IdentityAccessClient identityAccessClient;

    public PawnTicketAuthenticationFilter(IdentityAccessClient identityAccessClient) {
        this.identityAccessClient = identityAccessClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String tokenValue = authorization.substring(7);
            identityAccessClient.currentUser(tokenValue).ifPresentOrElse(currentUser -> {
                        AuthenticatedPawnTicketUser principal = new AuthenticatedPawnTicketUser(
                                currentUser.id(),
                                currentUser.actorUserId(),
                                currentUser.tenantId(),
                                currentUser.impersonating(),
                                currentUser.permissions()
                        );
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, tokenValue, List.of())
                        );
                    },
                    () -> log.warn("unable to authenticate bearer token for {} {}", request.getMethod(), request.getRequestURI())
            );
        }

        filterChain.doFilter(request, response);
    }
}
