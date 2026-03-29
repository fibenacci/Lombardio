Feature: Access Governance
  As the Lombardio platform
  I want platform and tenant responsibilities to be clearly separated
  So that tenant admins can manage their own staff without becoming platform admins

  Scenario: Platform administrators govern tenants across the platform
    Given a platform administrator without a tenant scope
    When platform access is evaluated for tenant "tenant-default"
    Then tenant and feature management should be allowed
    And tenant user and role management should be allowed

  Scenario: Tenant administrators govern users and roles only inside their own tenant
    Given a tenant administrator for tenant "tenant-default"
    When tenant access is evaluated for tenant "tenant-default"
    Then tenant user and role management should be allowed
    But tenant and feature management should not be allowed

  Scenario: Tenant administrators cannot manage another tenant
    Given a tenant administrator for tenant "tenant-default"
    When tenant access is evaluated for tenant "tenant-hamburg"
    Then tenant user and role management should not be allowed
