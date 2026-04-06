package io.lombardio.platform.tenant.application;

public record UpdateTenantCommand(String key, String displayName, String status) {}
