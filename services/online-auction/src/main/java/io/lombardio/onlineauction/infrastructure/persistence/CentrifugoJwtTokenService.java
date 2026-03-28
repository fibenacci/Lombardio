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
package io.lombardio.onlineauction.infrastructure.persistence;

import io.lombardio.onlineauction.config.CentrifugoProperties;
import io.lombardio.onlineauction.domain.RealtimeSession;
import io.lombardio.onlineauction.domain.RealtimeSessionTokenService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class CentrifugoJwtTokenService implements RealtimeSessionTokenService {

  private final CentrifugoProperties properties;

  public CentrifugoJwtTokenService(CentrifugoProperties properties) {
    this.properties = properties;
  }

  @Override
  public RealtimeSession createSession(String subject, String channel) {
    long exp = Instant.now().plusSeconds(3600).getEpochSecond();
    String connectionToken = sign("{\"sub\":\"" + subject + "\",\"exp\":" + exp + "}");
    String subscriptionToken =
        sign("{\"sub\":\"" + subject + "\",\"channel\":\"" + channel + "\",\"exp\":" + exp + "}");
    return new RealtimeSession(properties.wsUrl(), channel, connectionToken, subscriptionToken);
  }

  private String sign(String payloadJson) {
    String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
    String payload = base64Url(payloadJson);
    String content = header + "." + payload;
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(
          new SecretKeySpec(
              properties.hmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      String signature =
          Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
      return content + "." + signature;
    } catch (Exception exception) {
      throw new IllegalStateException("Could not sign Centrifugo token", exception);
    }
  }

  private String base64Url(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}
