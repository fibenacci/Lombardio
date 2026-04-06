package io.lombardio.platform.auth.application;

import io.lombardio.platform.security.AuthenticatedUser;

public interface OperatorTokenDecoder {

  AuthenticatedUser decode(String accessToken);
}
