package io.lombardio.identityaccess.auth.application;

import io.lombardio.identityaccess.auth.api.CurrentUserResponse;
import io.lombardio.identityaccess.auth.api.CreateDelegationRequest;
import io.lombardio.identityaccess.auth.api.LoginRequest;
import io.lombardio.identityaccess.auth.api.LoginResponse;
import io.lombardio.identityaccess.auth.api.TotpEnrollmentResponse;
import io.lombardio.identityaccess.auth.api.ActivateTotpRequest;
import io.lombardio.identityaccess.auth.api.VerifyTotpChallengeRequest;
import io.lombardio.identityaccess.access.application.AccessService;
import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.access.domain.UserRepository;
import io.lombardio.identityaccess.auth.domain.MfaChallenge;
import io.lombardio.identityaccess.auth.domain.MfaChallengeRepository;
import io.lombardio.identityaccess.auth.domain.SessionToken;
import io.lombardio.identityaccess.auth.domain.SessionTokenRepository;
import io.lombardio.identityaccess.auth.domain.TotpCredential;
import io.lombardio.identityaccess.auth.domain.TotpCredentialRepository;
import io.lombardio.identityaccess.auth.security.AuthenticatedUserPrincipal;
import io.lombardio.identityaccess.bootstrap.SeedFixtures;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final TotpCredentialRepository totpCredentialRepository;
    private final MfaChallengeRepository mfaChallengeRepository;
    private final AccessService accessService;
    private final PasswordEncoder passwordEncoder;
    private final TotpCodeService totpCodeService;
    private final SecretCipher secretCipher;
    private final Clock clock;

    public AuthService(
            UserRepository userRepository,
            SessionTokenRepository sessionTokenRepository,
            TotpCredentialRepository totpCredentialRepository,
            MfaChallengeRepository mfaChallengeRepository,
            AccessService accessService,
            PasswordEncoder passwordEncoder,
            TotpCodeService totpCodeService,
            SecretCipher secretCipher,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.totpCredentialRepository = totpCredentialRepository;
        this.mfaChallengeRepository = mfaChallengeRepository;
        this.accessService = accessService;
        this.passwordEncoder = passwordEncoder;
        this.totpCodeService = totpCodeService;
        this.secretCipher = secretCipher;
        this.clock = clock;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (request.tenantKey() != null
                && !request.tenantKey().isBlank()
                && !SeedFixtures.DEFAULT_TENANT_KEY.equals(request.tenantKey())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (isTotpEnabled(user.id())) {
            return issueMfaChallenge(user);
        }

        sessionTokenRepository.deleteByUserId(user.id());
        SessionToken sessionToken = issueSession(user, user);

        return toLoginResponse(sessionToken, user);
    }

    public LoginResponse verifyTotpChallenge(VerifyTotpChallengeRequest request) {
        MfaChallenge challenge = mfaChallengeRepository.findById(request.challengeId())
                .orElseThrow(() -> new BadCredentialsException("Invalid MFA challenge"));

        if (challenge.isExpired(Instant.now(clock))) {
            mfaChallengeRepository.deleteById(challenge.id());
            throw new BadCredentialsException("Invalid MFA challenge");
        }

        TotpCredential credential = totpCredentialRepository.findByUserId(challenge.userId())
                .filter(TotpCredential::enabled)
                .orElseThrow(() -> new BadCredentialsException("Invalid MFA challenge"));

        String secret = secretCipher.decrypt(credential.secretCiphertext());
        if (!totpCodeService.verifyCode(secret, request.code())) {
            throw new BadCredentialsException("Invalid MFA code");
        }

        User user = accessService.requireUser(challenge.userId());
        mfaChallengeRepository.deleteById(challenge.id());
        sessionTokenRepository.deleteByUserId(user.id());

        return toLoginResponse(issueSession(user, user), user);
    }

    public LoginResponse createDelegation(AuthenticatedUserPrincipal principal, CreateDelegationRequest request) {
        User actor = accessService.requireUser(principal.actorUserId());
        User target = accessService.requireUser(request.userId());

        if (!target.isActive()) {
            throw new IllegalArgumentException("Target user is inactive");
        }

        if (!canImpersonate(actor, target)) {
            throw new AccessDeniedException("Impersonation is not allowed");
        }

        SessionToken sessionToken = issueSession(actor, target);
        return toLoginResponse(sessionToken, target);
    }

    public void logout(String token) {
        sessionTokenRepository.deleteByToken(token);
    }

    public TotpEnrollmentResponse startTotpEnrollment(AuthenticatedUserPrincipal principal) {
        User user = accessService.requireUser(principal.userId());
        String secret = totpCodeService.generateSecret();
        Instant now = Instant.now(clock);

        totpCredentialRepository.save(new TotpCredential(
                user.id(),
                secretCipher.encrypt(secret),
                false,
                now,
                null
        ));

        return new TotpEnrollmentResponse(
                secret,
                buildOtpAuthUri(user.email(), secret),
                false
        );
    }

    public CurrentUserResponse activateTotp(AuthenticatedUserPrincipal principal, ActivateTotpRequest request) {
        User user = accessService.requireUser(principal.userId());
        TotpCredential credential = totpCredentialRepository.findByUserId(user.id())
                .orElseThrow(() -> new IllegalArgumentException("No pending TOTP enrollment"));

        String secret = secretCipher.decrypt(credential.secretCiphertext());
        if (!totpCodeService.verifyCode(secret, request.code())) {
            throw new BadCredentialsException("Invalid MFA code");
        }

        totpCredentialRepository.save(new TotpCredential(
                credential.userId(),
                credential.secretCiphertext(),
                true,
                credential.createdAt(),
                Instant.now(clock)
        ));

        return currentUser(principal);
    }

    public CurrentUserResponse currentUser(AuthenticatedUserPrincipal principal) {
        User user = accessService.requireUser(principal.userId());
        List<String> mfaMethods = mfaMethodsForUser(user.id());

        return new CurrentUserResponse(
                user.id(),
                principal.actorUserId(),
                user.tenantId(),
                user.username(),
                user.email(),
                user.displayName(),
                user.status(),
                principal.impersonating(),
                !mfaMethods.isEmpty(),
                mfaMethods,
                accessService.roleKeysForUser(user.id()),
                accessService.permissionsForUser(user.id())
        );
    }

    private boolean canImpersonate(User actor, User target) {
        List<String> actorPermissions = accessService.permissionsForUser(actor.id());

        if (actorPermissions.contains("sessions.impersonate.platform")) {
            return true;
        }

        return actorPermissions.contains("sessions.impersonate.tenant")
                && actor.tenantId().equals(target.tenantId());
    }

    private SessionToken issueSession(User actor, User effectiveUser) {
        return sessionTokenRepository.save(new SessionToken(
                UUID.randomUUID().toString(),
                actor.id(),
                effectiveUser.id(),
                effectiveUser.tenantId(),
                Instant.now(clock)
        ));
    }

    private LoginResponse toLoginResponse(SessionToken sessionToken, User effectiveUser) {
        List<String> permissions = accessService.permissionsForUser(effectiveUser.id());

        return new LoginResponse(
                "AUTHENTICATED",
                sessionToken.token(),
                null,
                "Bearer",
                effectiveUser.id(),
                sessionToken.actorUserId(),
                effectiveUser.tenantId(),
                effectiveUser.displayName(),
                !sessionToken.actorUserId().equals(effectiveUser.id()),
                permissions,
                mfaMethodsForUser(effectiveUser.id())
        );
    }

    private LoginResponse issueMfaChallenge(User user) {
        Instant now = Instant.now(clock);
        mfaChallengeRepository.deleteByUserId(user.id());
        MfaChallenge challenge = mfaChallengeRepository.save(new MfaChallenge(
                UUID.randomUUID().toString(),
                user.id(),
                user.tenantId(),
                "TOTP",
                now,
                now.plusSeconds(300)
        ));

        return new LoginResponse(
                "MFA_REQUIRED",
                null,
                challenge.id(),
                null,
                user.id(),
                user.id(),
                user.tenantId(),
                user.displayName(),
                false,
                List.of(),
                List.of("TOTP")
        );
    }

    private boolean isTotpEnabled(String userId) {
        return totpCredentialRepository.findByUserId(userId)
                .map(TotpCredential::enabled)
                .orElse(false);
    }

    private List<String> mfaMethodsForUser(String userId) {
        return isTotpEnabled(userId) ? List.of("TOTP") : List.of();
    }

    private String buildOtpAuthUri(String email, String secret) {
        return "otpauth://totp/Lombardio:" + email + "?secret=" + secret + "&issuer=Lombardio";
    }
}
