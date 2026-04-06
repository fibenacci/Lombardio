package io.lombardio.platform.auth.application;

public interface OperatorIdentityProvider {

  OperatorIdentityTokens login(String email, String password);

  OperatorIdentityTokens refresh(String refreshToken);

  void logout(String refreshToken);
}
