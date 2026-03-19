package io.lombardio.customer.portal.infrastructure.security;

import io.lombardio.customer.portal.application.CustomerPortalService;
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
public class CustomerPortalAuthenticationFilter extends OncePerRequestFilter {

    private final CustomerPortalService customerPortalService;

    public CustomerPortalAuthenticationFilter(CustomerPortalService customerPortalService) {
        this.customerPortalService = customerPortalService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/v1/customer-portal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String tokenValue = authorization.substring(7);
            AuthenticatedCustomerPortalUser principal = customerPortalService.authenticate(tokenValue);
            if (principal != null) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, tokenValue, List.of())
                );
            }
        }

        filterChain.doFilter(request, response);
    }
}
