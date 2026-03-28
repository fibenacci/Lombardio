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
package io.lombardio.pawnticket.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.data")
public class DemoDataProperties {

  private boolean enabled = true;
  private String scale = "medium";
  private String serviceScale;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getScale() {
    return scale;
  }

  public void setScale(String scale) {
    this.scale = scale;
  }

  public String getServiceScale() {
    return serviceScale;
  }

  public void setServiceScale(String serviceScale) {
    this.serviceScale = serviceScale;
  }

  public String effectiveScale() {
    return serviceScale == null || serviceScale.isBlank() ? scale : serviceScale;
  }
}
