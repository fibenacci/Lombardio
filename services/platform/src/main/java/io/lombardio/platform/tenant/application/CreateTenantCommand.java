package io.lombardio.platform.tenant.application;

public record CreateTenantCommand(String key, String displayName, String status) {}
