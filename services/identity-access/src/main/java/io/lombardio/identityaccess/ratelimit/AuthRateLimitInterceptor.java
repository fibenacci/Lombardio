package io.lombardio.identityaccess.ratelimit;

import io.lombardio.identityaccess.auth.security.AuthenticatedUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthRateLimitInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String TOTP_VERIFY_PATH = "/api/v1/auth/mfa/totp/verify";
    private static final String DELEGATIONS_PATH = "/api/v1/auth/delegations";

    private final RateLimitService rateLimitService;

    public AuthRateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();
        if (LOGIN_PATH.equals(requestUri)) {
            rateLimitService.enforceLoginLimit(clientIp(request));
            return true;
        }

        if (TOTP_VERIFY_PATH.equals(requestUri)) {
            rateLimitService.enforceTotpVerifyLimit(clientIp(request));
            return true;
        }

        if (DELEGATIONS_PATH.equals(requestUri)) {
            rateLimitService.enforceDelegationLimit(delegationClientKey(request));
        }

        return true;
    }

    private String delegationClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
            return "user:" + principal.actorUserId();
        }
        return "ip:" + clientIp(request);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
