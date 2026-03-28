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
package io.lombardio.identity.infrastructure.http;

import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.ExternalCrmConnector;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NoopExternalCrmConnector implements ExternalCrmConnector {

  @Override
  public boolean supports(String tenantId) {
    return false;
  }

  @Override
  public List<Customer> search(String tenantId, String query) {
    return List.of();
  }
}
