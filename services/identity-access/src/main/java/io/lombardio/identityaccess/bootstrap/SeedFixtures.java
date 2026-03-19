package io.lombardio.identityaccess.bootstrap;

import io.lombardio.identityaccess.access.domain.Permission;
import io.lombardio.identityaccess.access.domain.Branch;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SeedFixtures {

    public record DemoTenant(
            String id,
            String key,
            String label,
            String city,
            String branchOneKey,
            String branchOneLabel,
            String branchTwoKey,
            String branchTwoLabel
    ) {
    }

    public static final DemoTenant DEFAULT_TENANT = new DemoTenant(
            "tenant-default",
            "default",
            "Default Tenant",
            "Berlin",
            "hq",
            "Headquarters",
            "friedrichshain",
            "Friedrichshain"
    );
    public static final String PLATFORM_TENANT_ID = "tenant-platform";
    public static final String PLATFORM_TENANT_KEY = "platform";
    public static final String DEFAULT_TENANT_ID = DEFAULT_TENANT.id();
    public static final String DEFAULT_TENANT_KEY = DEFAULT_TENANT.key();
    public static final String DEFAULT_BRANCH_ID = branchId(DEFAULT_TENANT, DEFAULT_TENANT.branchOneKey());
    public static final String DEFAULT_BRANCH_KEY = DEFAULT_TENANT.branchOneKey();

    private static final List<DemoTenant> BUSINESS_TENANTS = List.of(
            DEFAULT_TENANT,
            new DemoTenant("tenant-hamburg", "hanseatic", "Hanseatic Pawn Hamburg", "Hamburg", "innenstadt", "Innenstadt", "altona", "Altona"),
            new DemoTenant("tenant-munich", "isar", "Isar Pfand Muenchen", "Muenchen", "maxvorstadt", "Maxvorstadt", "sendling", "Sendling"),
            new DemoTenant("tenant-cologne", "rhein", "Rhein Pfand Koeln", "Koeln", "innenstadt", "Innenstadt", "ehrenfeld", "Ehrenfeld"),
            new DemoTenant("tenant-stuttgart", "neckar", "Neckar Pfand Stuttgart", "Stuttgart", "mitte", "Mitte", "bad-cannstatt", "Bad Cannstatt")
    );

    public static final List<Permission> PERMISSIONS = List.of(
            new Permission("platform.tenants.read", "Read tenants", "Allows reading platform tenant records"),
            new Permission("platform.tenants.write", "Write tenants", "Allows creating and updating tenant records"),
            new Permission("users.read", "Read users", "Allows reading user records"),
            new Permission("users.write", "Write users", "Allows creating and updating users"),
            new Permission("branches.read", "Read branches", "Allows reading tenant branch records"),
            new Permission("branches.write", "Write branches", "Allows creating and updating tenant branch records"),
            new Permission("roles.read", "Read roles", "Allows reading role definitions"),
            new Permission("roles.write", "Write roles", "Allows creating and updating roles"),
            new Permission("permissions.read", "Read permissions", "Allows reading permission definitions"),
            new Permission("customers.read", "Read customers", "Allows reading customer records"),
            new Permission("customers.write", "Write customers", "Allows creating and updating customer records"),
            new Permission("aml.read", "Read AML", "Allows reading AML assessments and cases"),
            new Permission("aml.write", "Write AML", "Allows creating and updating AML assessments and cases"),
            new Permission("kyc.read", "Read KYC", "Allows reading KYC records"),
            new Permission("kyc.write", "Write KYC", "Allows creating and updating KYC records"),
            new Permission("loans.read", "Read loans", "Allows reading valuation guidelines and loan records"),
            new Permission("loans.write", "Write loans", "Allows creating and updating loan records"),
            new Permission("pawn-tickets.read", "Read pawn tickets", "Allows reading pawn ticket records and documents"),
            new Permission("pawn-tickets.write", "Write pawn tickets", "Allows issuing and updating pawn ticket records"),
            new Permission("cash-transactions.read", "Read cash transactions", "Allows reading cash desk journal records"),
            new Permission("cash-transactions.write", "Write cash transactions", "Allows executing cash desk transactions"),
            new Permission("auctions.read", "Read auctions", "Allows reading liquidation and auction records"),
            new Permission("auctions.write", "Write auctions", "Allows managing liquidation, auctions and bids"),
            new Permission("online-auctions.read", "Read online auctions", "Allows reading online auction catalogue and bidder activity"),
            new Permission("online-auctions.write", "Write online auctions", "Allows managing online auction catalogue and live bidding"),
            new Permission("reporting.read", "Read reporting", "Allows reading tenant financial and inventory reporting dashboards"),
            new Permission("audit.read", "Read audit events", "Allows reading audit events"),
            new Permission("sessions.impersonate.platform", "Impersonate across tenants", "Allows support sessions across tenants"),
            new Permission("sessions.impersonate.tenant", "Impersonate within tenant", "Allows support sessions within the current tenant")
    );

    public static final Role PLATFORM_ADMIN_ROLE = platformRoles().get(0);
    public static final Role ADMIN_ROLE = rolesForTenant(DEFAULT_TENANT).get(0);
    public static final Role REVIEW_ROLE = rolesForTenant(DEFAULT_TENANT).get(4);

    private SeedFixtures() {
    }

    public static List<DemoTenant> businessTenants(String scale) {
        int count = switch (scale == null ? "medium" : scale.trim().toLowerCase()) {
            case "small" -> 2;
            case "large" -> BUSINESS_TENANTS.size();
            default -> 4;
        };
        return BUSINESS_TENANTS.subList(0, count);
    }

    public static int usersPerTenant(String scale) {
        return switch (scale == null ? "medium" : scale.trim().toLowerCase()) {
            case "small" -> 8;
            case "large" -> 28;
            default -> 16;
        };
    }

    public static List<Role> platformRoles() {
        return List.of(
                new Role(
                        "role-platform-admin",
                        PLATFORM_TENANT_ID,
                        "platform-admin",
                        "Platform Administrator",
                        "Platform-wide tenant and support administration",
                        true,
                        List.of(
                                "platform.tenants.read",
                                "platform.tenants.write",
                                "users.read",
                                "users.write",
                                "branches.read",
                                "branches.write",
                                "roles.read",
                                "roles.write",
                                "permissions.read",
                                "customers.read",
                                "customers.write",
                                "aml.read",
                                "aml.write",
                                "kyc.read",
                                "kyc.write",
                                "loans.read",
                                "loans.write",
                                "pawn-tickets.read",
                                "pawn-tickets.write",
                                "cash-transactions.read",
                                "cash-transactions.write",
                                "auctions.read",
                                "auctions.write",
                                "online-auctions.read",
                                "online-auctions.write",
                                "reporting.read",
                                "audit.read",
                                "sessions.impersonate.platform"
                        )
                ),
                new Role(
                        "role-platform-auditor",
                        PLATFORM_TENANT_ID,
                        "platform-auditor",
                        "Platform Auditor",
                        "Read-only oversight across tenants",
                        true,
                        List.of("platform.tenants.read", "users.read", "roles.read", "permissions.read", "audit.read", "reporting.read")
                )
        );
    }

    public static List<Role> rolesForTenant(DemoTenant tenant) {
        return List.of(
                new Role(roleId(tenant, "admin"), tenant.id(), "admin", "Administrator", "Full administrative access", true, tenantAdminPermissions()),
                new Role(roleId(tenant, "ops"), tenant.id(), "ops-manager", "Operations", "Operational branch management", true, operationsPermissions()),
                new Role(roleId(tenant, "compliance"), tenant.id(), "compliance", "Compliance", "AML and KYC review", true, compliancePermissions()),
                new Role(roleId(tenant, "cash"), tenant.id(), "cashier", "Cash Desk", "Cash desk and repayment handling", true, cashDeskPermissions()),
                new Role(roleId(tenant, "review"), tenant.id(), "review", "Review", "Read-only operational review", true, reviewPermissions())
        );
    }

    public static List<Branch> branchesForTenant(DemoTenant tenant, Instant timestamp) {
        return List.of(
                new Branch(branchId(tenant, tenant.branchOneKey()), tenant.id(), tenant.branchOneKey(), tenant.branchOneLabel(), "ACTIVE", timestamp, timestamp),
                new Branch(branchId(tenant, tenant.branchTwoKey()), tenant.id(), tenant.branchTwoKey(), tenant.branchTwoLabel(), "ACTIVE", timestamp.plusSeconds(300), timestamp.plusSeconds(300))
        );
    }

    public static List<User> platformUsers(String passwordHash, Instant timestamp) {
        return List.of(
                new User(
                        "user-platform-admin",
                        PLATFORM_TENANT_ID,
                        List.of(),
                        "platform-admin",
                        "platform@lombardio.local",
                        passwordHash,
                        "Platform Admin",
                        "ACTIVE",
                        List.of("role-platform-admin"),
                        timestamp,
                        timestamp
                ),
                new User(
                        "user-platform-auditor",
                        PLATFORM_TENANT_ID,
                        List.of(),
                        "platform-auditor",
                        "audit@lombardio.local",
                        passwordHash,
                        "Platform Audit",
                        "ACTIVE",
                        List.of("role-platform-auditor"),
                        timestamp.plusSeconds(600),
                        timestamp.plusSeconds(600)
                )
        );
    }

    public static List<User> usersForTenant(DemoTenant tenant, String passwordHash, int count, Instant baseTimestamp) {
        List<User> users = new ArrayList<>();
        users.add(primaryAdminUser(tenant, passwordHash, baseTimestamp));

        String[] firstNames = {"Anna", "Murat", "Leonie", "Jonas", "Sofia", "Mila", "Emre", "Paul", "Nina", "David", "Lina", "Felix"};
        String[] lastNames = {"Becker", "Yilmaz", "Schmidt", "Kaya", "Wagner", "Hartmann", "Keller", "Nguyen", "Fischer", "Ali", "Scholz", "Krause"};
        List<String> roleCycle = List.of(
                roleId(tenant, "ops"),
                roleId(tenant, "compliance"),
                roleId(tenant, "cash"),
                roleId(tenant, "review")
        );

        for (int index = 1; index < count; index++) {
            String firstName = firstNames[(index + tenant.key().length()) % firstNames.length];
            String lastName = lastNames[(index * 2 + tenant.label().length()) % lastNames.length];
            String username = tenant.key() + "-user-" + String.format("%02d", index);
            String email = username + "@lombardio.local";
            String status = index % 9 == 0 ? "INACTIVE" : "ACTIVE";
            List<String> branchIds = index % 2 == 0
                    ? List.of(branchId(tenant, tenant.branchOneKey()))
                    : List.of(branchId(tenant, tenant.branchTwoKey()));
            if (index % 7 == 0) {
                branchIds = List.of(branchId(tenant, tenant.branchOneKey()), branchId(tenant, tenant.branchTwoKey()));
            }

            users.add(new User(
                    "user-" + tenant.key() + "-" + String.format("%02d", index),
                    tenant.id(),
                    branchIds,
                    username,
                    email,
                    passwordHash,
                    firstName + " " + lastName,
                    status,
                    List.of(roleCycle.get((index - 1) % roleCycle.size())),
                    baseTimestamp.plusSeconds(index * 900L),
                    baseTimestamp.plusSeconds(index * 900L)
            ));
        }

        return users;
    }

    private static User primaryAdminUser(DemoTenant tenant, String passwordHash, Instant timestamp) {
        String id = tenant.id().equals(DEFAULT_TENANT.id()) ? "user-admin" : "user-" + tenant.key() + "-admin";
        String username = tenant.id().equals(DEFAULT_TENANT.id()) ? "admin" : tenant.key() + "-admin";
        String email = tenant.id().equals(DEFAULT_TENANT.id()) ? "admin@lombardio.local" : "admin@" + tenant.key() + ".lombardio.local";
        return new User(
                id,
                tenant.id(),
                List.of(branchId(tenant, tenant.branchOneKey())),
                username,
                email,
                passwordHash,
                tenant.label() + " Admin",
                "ACTIVE",
                List.of(roleId(tenant, "admin")),
                timestamp,
                timestamp
        );
    }

    private static List<String> tenantAdminPermissions() {
        return List.of(
                "users.read", "users.write",
                "branches.read", "branches.write",
                "roles.read", "roles.write",
                "permissions.read",
                "customers.read", "customers.write",
                "aml.read", "aml.write",
                "kyc.read", "kyc.write",
                "loans.read", "loans.write",
                "pawn-tickets.read", "pawn-tickets.write",
                "cash-transactions.read", "cash-transactions.write",
                "auctions.read", "auctions.write",
                "online-auctions.read", "online-auctions.write",
                "reporting.read",
                "audit.read",
                "sessions.impersonate.tenant"
        );
    }

    private static List<String> operationsPermissions() {
        return List.of(
                "users.read",
                "branches.read",
                "customers.read", "customers.write",
                "loans.read", "loans.write",
                "pawn-tickets.read", "pawn-tickets.write",
                "cash-transactions.read", "cash-transactions.write",
                "auctions.read", "auctions.write",
                "online-auctions.read",
                "reporting.read"
        );
    }

    private static List<String> compliancePermissions() {
        return List.of(
                "customers.read",
                "aml.read", "aml.write",
                "kyc.read", "kyc.write",
                "loans.read",
                "pawn-tickets.read",
                "audit.read",
                "reporting.read"
        );
    }

    private static List<String> cashDeskPermissions() {
        return List.of(
                "customers.read",
                "loans.read",
                "pawn-tickets.read", "pawn-tickets.write",
                "cash-transactions.read", "cash-transactions.write",
                "reporting.read"
        );
    }

    private static List<String> reviewPermissions() {
        return List.of(
                "users.read",
                "roles.read",
                "permissions.read",
                "customers.read",
                "aml.read",
                "kyc.read",
                "loans.read",
                "pawn-tickets.read",
                "cash-transactions.read",
                "auctions.read",
                "online-auctions.read",
                "reporting.read",
                "audit.read"
        );
    }

    private static String roleId(DemoTenant tenant, String suffix) {
        return "role-" + tenant.key() + "-" + suffix;
    }

    private static String branchId(DemoTenant tenant, String branchKey) {
        return "branch-" + tenant.key() + "-" + branchKey;
    }

    public static Branch defaultBranch() {
        Instant timestamp = Instant.now().minusSeconds(86_400);
        return branchesForTenant(DEFAULT_TENANT, timestamp).get(0);
    }

    public static User adminUser(String passwordHash) {
        Instant timestamp = Instant.now().minusSeconds(86_400);
        return primaryAdminUser(DEFAULT_TENANT, passwordHash, timestamp);
    }

    public static User platformAdminUser(String passwordHash) {
        Instant timestamp = Instant.now().minusSeconds(86_400);
        return platformUsers(passwordHash, timestamp).get(0);
    }
}
