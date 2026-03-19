package io.lombardio.kyc.demo;

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
