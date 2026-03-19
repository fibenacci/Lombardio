package io.lombardio.identityaccess.auth.application;

import io.lombardio.identityaccess.access.application.AccessService;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.User;
import io.lombardio.identityaccess.auth.api.CreateDelegationRequest;
import io.lombardio.identityaccess.auth.api.LoginRequest;
import io.lombardio.identityaccess.auth.api.VerifyTotpChallengeRequest;
import io.lombardio.identityaccess.auth.security.AuthenticatedUserPrincipal;
import io.lombardio.identityaccess.auth.domain.TotpCredential;
import io.lombardio.identityaccess.bootstrap.SeedFixtures;
import io.lombardio.identityaccess.support.InMemoryRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private final InMemoryRepositories.Users users = new InMemoryRepositories.Users();
    private final InMemoryRepositories.Roles roles = new InMemoryRepositories.Roles();
    private final InMemoryRepositories.Branches branches = new InMemoryRepositories.Branches();
    private final InMemoryRepositories.Permissions permissions = new InMemoryRepositories.Permissions();
    private final InMemoryRepositories.Sessions sessions = new InMemoryRepositories.Sessions();
    private final InMemoryRepositories.TotpCredentials totpCredentials = new InMemoryRepositories.TotpCredentials();
    private final InMemoryRepositories.Challenges challenges = new InMemoryRepositories.Challenges();
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-18T10:15:30Z"), ZoneOffset.UTC);
    private final TotpCodeService totpCodeService = new TotpCodeService(clock);
    private final SecretCipher secretCipher = new SecretCipher("5t0iQ0q4ryx6vSlvBo5J+eqT7vJk2mW+NfO7M3h/r90=");

    private AuthService authService;

    @BeforeEach
    void setUp() {
        SeedFixtures.PERMISSIONS.forEach(permissions::save);
        roles.save(SeedFixtures.PLATFORM_ADMIN_ROLE);
        roles.save(SeedFixtures.ADMIN_ROLE);
        users.save(SeedFixtures.adminUser(passwordEncoder.encode("change-me")));
        users.save(SeedFixtures.platformAdminUser(passwordEncoder.encode("change-me")));

        AccessService accessService = new AccessService(users, roles, branches, permissions, sessions, passwordEncoder, clock);
        authService = new AuthService(users, sessions, totpCredentials, challenges, accessService, passwordEncoder, totpCodeService, secretCipher, clock);
    }

    @Test
    void shouldIssueSessionTokenForValidCredentials() {
        var response = authService.login(new LoginRequest(SeedFixtures.DEFAULT_TENANT_KEY, "admin@lombardio.local", "change-me"));

        assertEquals("AUTHENTICATED", response.status());
        assertEquals("user-admin", response.userId());
        assertEquals(SeedFixtures.DEFAULT_TENANT_ID, response.tenantId());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1, sessions.size());
    }

    @Test
    void shouldRequireTotpForUserWithEnabledFactor() {
        String secret = totpCodeService.generateSecret();
        totpCredentials.save(new TotpCredential(
                "user-admin",
                secretCipher.encrypt(secret),
                true,
                Instant.parse("2026-03-18T09:00:00Z"),
                Instant.parse("2026-03-18T09:01:00Z")
        ));

        var response = authService.login(new LoginRequest(SeedFixtures.DEFAULT_TENANT_KEY, "admin@lombardio.local", "change-me"));

        assertEquals("MFA_REQUIRED", response.status());
        assertEquals("user-admin", response.userId());
        assertEquals(List.of("TOTP"), response.mfaMethods());
        assertEquals(0, sessions.size());
        assertEquals(1, challenges.size());
    }

    @Test
    void shouldVerifyTotpChallengeAndIssueSession() {
        String secret = totpCodeService.generateSecret();
        totpCredentials.save(new TotpCredential(
                "user-admin",
                secretCipher.encrypt(secret),
                true,
                Instant.parse("2026-03-18T09:00:00Z"),
                Instant.parse("2026-03-18T09:01:00Z")
        ));

        var challenge = authService.login(new LoginRequest(SeedFixtures.DEFAULT_TENANT_KEY, "admin@lombardio.local", "change-me"));
        var verified = authService.verifyTotpChallenge(new VerifyTotpChallengeRequest(
                challenge.challengeId(),
                totpCodeService.currentCode(secret)
        ));

        assertEquals("AUTHENTICATED", verified.status());
        assertEquals("Bearer", verified.tokenType());
        assertEquals(1, sessions.size());
    }

    @Test
    void shouldCreateDelegatedSessionForPlatformAdmin() {
        var principal = new AuthenticatedUserPrincipal(
                "user-platform-admin",
                "user-platform-admin",
                SeedFixtures.PLATFORM_TENANT_ID,
                "platform-admin",
                false,
                "token-platform",
                List.of(new SimpleGrantedAuthority("PERMISSION_sessions.impersonate.platform"))
        );

        var response = authService.createDelegation(principal, new CreateDelegationRequest("user-admin"));

        assertEquals("user-admin", response.userId());
        assertEquals("user-platform-admin", response.actorUserId());
        assertEquals(SeedFixtures.DEFAULT_TENANT_ID, response.tenantId());
    }

    @Test
    void shouldRejectCrossTenantDelegationForTenantAdmin() {
        users.save(new User(
                "user-other-tenant",
                "tenant-other",
                List.of(),
                "branch-user",
                "branch-user@lombardio.local",
                passwordEncoder.encode("change-me"),
                "Branch User",
                "ACTIVE",
                List.of(SeedFixtures.REVIEW_ROLE.id()),
                Instant.parse("2026-03-18T00:00:00Z"),
                Instant.parse("2026-03-18T00:00:00Z")
        ));

        var principal = new AuthenticatedUserPrincipal(
                "user-admin",
                "user-admin",
                SeedFixtures.DEFAULT_TENANT_ID,
                "admin",
                false,
                "token-tenant-admin",
                List.of(new SimpleGrantedAuthority("PERMISSION_sessions.impersonate.tenant"))
        );

        assertThrows(AccessDeniedException.class, () ->
                authService.createDelegation(principal, new CreateDelegationRequest("user-other-tenant")));
    }

    @Test
    void shouldRejectInvalidPassword() {
        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequest(SeedFixtures.DEFAULT_TENANT_KEY, "admin@lombardio.local", "wrong-pass")));
    }
}
