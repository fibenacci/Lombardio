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
package io.lombardio.platform.auth.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.iam.application.IdentityProviderUnavailableException;
import io.lombardio.platform.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

@Service
public class OperatorAuthService {

  private final OperatorIdentityProvider identityProvider;
  private final OperatorTokenDecoder tokenDecoder;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Standard Spring dependency injection")
  public OperatorAuthService(
      OperatorIdentityProvider identityProvider, OperatorTokenDecoder tokenDecoder) {
    this.identityProvider = identityProvider;
    this.tokenDecoder = tokenDecoder;
  }

  public OperatorSession login(String email, String password) {
    return toOperatorSession(identityProvider.login(email, password));
  }

  public OperatorSession refresh(String refreshToken) {
    return toOperatorSession(identityProvider.refresh(refreshToken));
  }

  public void logout(String refreshToken) {
    identityProvider.logout(refreshToken);
  }

  public AuthenticatedUser authenticatedUserFromAccessToken(String accessToken) {
    return tokenDecoder.decode(accessToken);
  }

  public OperatorSessionUserView profileFromAccessToken(String accessToken) {
    AuthenticatedUser user = authenticatedUserFromAccessToken(accessToken);
    return OperatorSessionUserView.fromAuthenticatedUser(user);
  }

  private OperatorSession toOperatorSession(OperatorIdentityTokens response) {
    if (response == null
        || response.accessToken() == null
        || response.accessToken().isBlank()
        || response.refreshToken() == null
        || response.refreshToken().isBlank()) {
      throw new IdentityProviderUnavailableException(
          "Operator authentication failed", new IllegalStateException("Incomplete token response"));
    }

    OperatorSessionUserView user = profileFromAccessToken(response.accessToken());
    return new OperatorSession(response.accessToken(), response.refreshToken(), user);
  }
}
