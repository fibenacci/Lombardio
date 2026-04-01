export type AccessPolicyUser = {
  permissions?: string[];
  tenantId?: string | null;
};

export function canManagePlatformTenants(user?: AccessPolicyUser | null): boolean {
  return Boolean(user?.permissions?.includes("platform.tenants.write"));
}

export function canReadPlatformTenants(user?: AccessPolicyUser | null): boolean {
  return Boolean(user?.permissions?.includes("platform.tenants.read"));
}

export function canManageTenantUsers(user?: AccessPolicyUser | null, tenantId?: string | null): boolean {
  if (!user || !tenantId) {
    return false;
  }

  if (canManagePlatformTenants(user)) {
    return true;
  }

  return user.tenantId === tenantId && user.permissions?.includes("users.write") === true;
}

export function canManageTenantRoles(user?: AccessPolicyUser | null, tenantId?: string | null): boolean {
  if (!user || !tenantId) {
    return false;
  }

  if (canManagePlatformTenants(user)) {
    return true;
  }

  return user.tenantId === tenantId && user.permissions?.includes("roles.write") === true;
}

export function canManageTenantFeatures(user?: AccessPolicyUser | null): boolean {
  return canManagePlatformTenants(user);
}
