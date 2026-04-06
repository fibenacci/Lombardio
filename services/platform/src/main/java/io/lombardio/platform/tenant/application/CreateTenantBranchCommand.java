package io.lombardio.platform.tenant.application;

public record CreateTenantBranchCommand(String key, String displayName, String status) {}
