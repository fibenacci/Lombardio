package io.lombardio.identityaccess.auth.api;

import io.lombardio.identityaccess.auth.application.AuthService;
import io.lombardio.identityaccess.auth.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/mfa/totp/verify")
    public LoginResponse verifyTotpChallenge(@Valid @RequestBody VerifyTotpChallengeRequest request) {
        return authService.verifyTotpChallenge(request);
    }

    @PostMapping("/delegations")
    public LoginResponse createDelegation(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateDelegationRequest request
    ) {
        return authService.createDelegation(principal, request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        authService.logout(principal.token());
    }

    @PostMapping("/mfa/totp/enroll")
    public TotpEnrollmentResponse startTotpEnrollment(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return authService.startTotpEnrollment(principal);
    }

    @PostMapping("/mfa/totp/activate")
    public CurrentUserResponse activateTotp(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody ActivateTotpRequest request
    ) {
        return authService.activateTotp(principal, request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return authService.currentUser(principal);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
