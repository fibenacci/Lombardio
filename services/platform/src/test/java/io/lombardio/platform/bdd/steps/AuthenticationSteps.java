/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.bdd.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.lombardio.platform.auth.application.Operator;
import io.lombardio.platform.auth.application.OperatorAuthService;
import io.lombardio.platform.auth.application.OperatorIdentityProvider;
import io.lombardio.platform.auth.application.OperatorIdentityTokens;
import io.lombardio.platform.auth.application.OperatorSession;
import io.lombardio.platform.auth.application.OperatorSessionCrypto;
import io.lombardio.platform.auth.application.OperatorSessionStore;
import io.lombardio.platform.auth.application.OperatorSessionUserView;
import io.lombardio.platform.auth.application.OperatorTokenDecoder;
import io.lombardio.platform.auth.application.PersistedOperatorSession;
import io.lombardio.platform.auth.application.StoredOperatorSessionService;
import io.lombardio.platform.config.OperatorSessionProperties;
import io.lombardio.platform.security.AuthenticatedUser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationSteps {

  private OperatorAuthService operatorAuthService;
  private StoredOperatorSessionService storedOperatorSessionService;
  private OperatorIdentityProvider identityProvider;
  private OperatorTokenDecoder tokenDecoder;
  private OperatorSessionStore sessionStore;

  private OperatorSession lastSession;
  private OperatorSessionUserView lastProfile;

  @Before
  public void setup() {
    identityProvider = mock(OperatorIdentityProvider.class);
    tokenDecoder = mock(OperatorTokenDecoder.class);
    sessionStore = new InMemorySessionStore();

    OperatorSessionProperties properties =
        new OperatorSessionProperties(
            "lombardio-session", "/", false, "Lax", 3600, "9p4w3v-v3ry-s3cr3t-t3st-k3y-32-ch");
    OperatorSessionCrypto crypto = new OperatorSessionCrypto(properties);

    operatorAuthService = new OperatorAuthService(identityProvider, tokenDecoder);
    storedOperatorSessionService =
        new StoredOperatorSessionService(sessionStore, crypto, operatorAuthService, properties);

    SecurityContextHolder.clearContext();
  }

  @When("I login with email {string} and password {string}")
  public void i_login_with_email_and_password(String email, String password) {
    OperatorIdentityTokens tokens =
        new OperatorIdentityTokens("access-" + email, "refresh-" + email);
    when(identityProvider.login(email, password)).thenReturn(tokens);

    // Mock token decoding to return a valid AuthenticatedUser (infra type)
    // which the service will then map to our Operator (domain type)
    AuthenticatedUser authUser =
        new AuthenticatedUser(
            "user-123", "user-123", "tenant-1", false, email, "Admin", List.of("read"));
    when(tokenDecoder.decode("access-" + email)).thenReturn(authUser);

    lastSession = storedOperatorSessionService.createSession(tokens);
  }

  @Then("I should receive a valid session ID")
  public void i_should_receive_a_valid_session_id() {
    Assertions.assertNotNull(lastSession.sessionId());
    Assertions.assertFalse(lastSession.sessionId().isBlank());
  }

  @Then("my profile should be available in the response")
  public void my_profile_should_be_available_in_the_response() {
    Assertions.assertNotNull(lastSession.user());
  }

  @Then("my display name should be {string}")
  public void my_display_name_should_be(String expectedName) {
    Assertions.assertEquals(expectedName, lastSession.user().displayName());
  }

  @Given("I am authenticated as {string} with display name {string}")
  public void i_am_authenticated_as_with_display_name(String email, String displayName) {
    Operator operator =
        new Operator(
            "user-123",
            "user-123",
            "tenant-1",
            false,
            email,
            displayName,
            List.of(),
            List.of("read"));

    // Fill security context like the filter would do
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(operator, "token-123", List.of()));
  }

  @When("I request my own profile information")
  public void i_request_my_own_profile_information() {
    // Simulate controller logic
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof Operator operator) {
      lastProfile = OperatorSessionUserView.fromOperator(operator);
    }
  }

  @Then("the system should return my display name {string} and email {string}")
  public void the_system_should_return_my_display_name_and_email(
      String expectedName, String expectedEmail) {
    Assertions.assertNotNull(lastProfile);
    Assertions.assertEquals(expectedName, lastProfile.displayName());
    Assertions.assertEquals(expectedEmail, lastProfile.email());
  }

  @Then("my assigned permissions should be included")
  public void my_assigned_permissions_should_be_included() {
    Assertions.assertNotNull(lastProfile.permissions());
    Assertions.assertFalse(lastProfile.permissions().isEmpty());
  }

  private static class InMemorySessionStore implements OperatorSessionStore {
    private final Map<String, PersistedOperatorSession> sessions = new HashMap<>();

    @Override
    public PersistedOperatorSession save(PersistedOperatorSession session) {
      sessions.put(session.id(), session);
      return session;
    }

    @Override
    public Optional<PersistedOperatorSession> findById(String id) {
      return Optional.ofNullable(sessions.get(id));
    }

    @Override
    public void deleteById(String id) {
      sessions.remove(id);
    }

    @Override
    public void deleteExpiredBefore(java.time.Instant expiry) {
      sessions.values().removeIf(s -> s.expiresAt().isBefore(expiry));
    }
  }
}
