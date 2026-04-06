package io.lombardio.platform.auth.infrastructure.security;

import io.lombardio.platform.auth.application.OperatorTokenDecoder;
import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.platform.security.UnauthorizedException;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtOperatorTokenDecoder implements OperatorTokenDecoder {

  private final JwtDecoder jwtDecoder;

  public JwtOperatorTokenDecoder(JwtDecoder jwtDecoder) {
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  public AuthenticatedUser decode(String accessToken) {
    try {
      Jwt jwt = jwtDecoder.decode(accessToken);
      List<String> permissions = extractPermissions(jwt);
      return new AuthenticatedUser(
          jwt.getSubject(),
          stringClaim(jwt, "actorUserId", jwt.getSubject()),
          stringClaim(jwt, "tenantId", null),
          booleanClaim(jwt, "impersonating"),
          stringClaim(jwt, "email", stringClaim(jwt, "preferred_username", "")),
          stringClaim(
              jwt,
              "name",
              stringClaim(jwt, "preferred_username", stringClaim(jwt, "email", jwt.getSubject()))),
          permissions);
    } catch (RuntimeException exception) {
      throw new UnauthorizedException("Invalid operator session");
    }
  }

  private List<String> extractPermissions(Jwt jwt) {
    Object realmAccess = jwt.getClaims().get("realm_access");
    if (realmAccess instanceof Map<?, ?> map) {
      Object roles = map.get("roles");
      if (roles instanceof List<?> list) {
        return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
      }
    }
    return List.of();
  }

  private String stringClaim(Jwt jwt, String claimName, String fallback) {
    Object value = jwt.getClaims().get(claimName);
    return value instanceof String stringValue ? stringValue : fallback;
  }

  private boolean booleanClaim(Jwt jwt, String claimName) {
    Object value = jwt.getClaims().get(claimName);
    return value instanceof Boolean booleanValue && booleanValue;
  }
}
