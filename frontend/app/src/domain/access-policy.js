export function canManagePlatformTenants(user) {
  return Boolean(user?.permissions?.includes("platform.tenants.write"));
}

export function canReadPlatformTenants(user) {
  return Boolean(user?.permissions?.includes("platform.tenants.read"));
}

export function canManageTenantUsers(user, tenantId) {
  if (!user || !tenantId) {
    return false;
  }

  if (canManagePlatformTenants(user)) {
    return true;
  }

  return user.tenantId === tenantId && user.permissions?.includes("users.write");
}

export function canManageTenantRoles(user, tenantId) {
  if (!user || !tenantId) {
    return false;
  }

  if (canManagePlatformTenants(user)) {
    return true;
  }

  return user.tenantId === tenantId && user.permissions?.includes("roles.write");
}

export function canManageTenantFeatures(user) {
  return canManagePlatformTenants(user);
}
