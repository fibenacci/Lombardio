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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.lombardio.platform.config.OperatorSessionProperties;
import org.junit.jupiter.api.Test;

class OperatorSessionCryptoTest {

  @Test
  void encryptsAndDecryptsValue() {
    OperatorSessionCrypto crypto =
        new OperatorSessionCrypto(
            new OperatorSessionProperties(
                "lombardio_operator_session", "/", false, "Lax", 2_592_000L, "0123456789abcdef"));

    String ciphertext = crypto.encrypt("access-token");

    assertNotEquals("access-token", ciphertext);
    assertEquals("access-token", crypto.decrypt(ciphertext));
  }
}
