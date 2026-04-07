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

  public OperatorIdentityTokens login(String email, String password) {
    return identityProvider.login(email, password);
  }

  public OperatorIdentityTokens refresh(String refreshToken) {
    return identityProvider.refresh(refreshToken);
  }

  public void logout(String refreshToken) {
    identityProvider.logout(refreshToken);
  }

  public Operator resolveOperator(String accessToken) {
    return toOperator(tokenDecoder.decode(accessToken));
  }

  public OperatorSessionUserView resolveProfile(String accessToken) {
    Operator operator = resolveOperator(accessToken);
    return OperatorSessionUserView.fromOperator(operator);
  }

  private Operator toOperator(AuthenticatedUser user) {
    return new Operator(
        user.userId(),
        user.actorUserId(),
        user.tenantId(),
        user.impersonating(),
        user.email(),
        user.displayName(),
        user.permissions(),
        user.permissions());
  }
}
