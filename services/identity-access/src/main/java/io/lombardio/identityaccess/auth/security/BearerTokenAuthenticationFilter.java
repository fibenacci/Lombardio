package io.lombardio.identityaccess.auth.security;

import io.lombardio.identityaccess.access.application.AccessService;
import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.auth.domain.SessionToken;
import io.lombardio.identityaccess.auth.domain.SessionTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final SessionTokenRepository sessionTokenRepository;
    private final AccessService accessService;

    public BearerTokenAuthenticationFilter(SessionTokenRepository sessionTokenRepository, AccessService accessService) {
        this.sessionTokenRepository = sessionTokenRepository;
        this.accessService = accessService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String tokenValue = authorization.substring(7);
            SessionToken sessionToken = sessionTokenRepository.findByToken(tokenValue).orElse(null);

            if (sessionToken != null) {
                User user = accessService.requireUser(sessionToken.userId());
                if (user.isActive()) {
                    List<SimpleGrantedAuthority> authorities = accessService.permissionsForUser(user.id()).stream()
                            .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))
                            .toList();

                    AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
                            sessionToken.actorUserId(),
                            user.id(),
                            sessionToken.tenantId(),
                            user.username(),
                            !sessionToken.actorUserId().equals(user.id()),
                            tokenValue,
                            authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(principal, tokenValue, authorities)
                    );
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
