package io.lombardio.identityaccess.bootstrap;

import io.lombardio.identityaccess.access.domain.Permission;
import io.lombardio.identityaccess.access.domain.Branch;
import io.lombardio.identityaccess.access.domain.Role;
import io.lombardio.identityaccess.access.domain.User;

import java.time.Instant;
import java.util.List;

public final class SeedFixtures {

    public static final String PLATFORM_TENANT_ID = "tenant-platform";
    public static final String PLATFORM_TENANT_KEY = "platform";
    public static final String DEFAULT_TENANT_ID = "tenant-default";
    public static final String DEFAULT_TENANT_KEY = "default";
    public static final String DEFAULT_BRANCH_ID = "branch-default-hq";
    public static final String DEFAULT_BRANCH_KEY = "hq";

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

    public static final Role PLATFORM_ADMIN_ROLE = new Role(
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
    );

    public static final Role ADMIN_ROLE = new Role(
            "role-admin",
            DEFAULT_TENANT_ID,
            "admin",
            "Administrator",
            "Full administrative access",
            true,
            List.of(
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
                    "sessions.impersonate.tenant"
            )
        );

    public static final Role REVIEW_ROLE = new Role(
            "role-review",
            DEFAULT_TENANT_ID,
            "review",
            "Review",
            "Read-only operational review role",
            true,
            List.of("users.read", "roles.read", "permissions.read", "customers.read", "aml.read", "kyc.read", "loans.read", "pawn-tickets.read", "cash-transactions.read", "auctions.read", "online-auctions.read", "reporting.read", "audit.read")
    );

    private SeedFixtures() {
    }

    public static Branch defaultBranch() {
        Instant now = Instant.parse("2026-03-18T00:00:00Z");
        return new Branch(
                DEFAULT_BRANCH_ID,
                DEFAULT_TENANT_ID,
                DEFAULT_BRANCH_KEY,
                "Headquarters",
                "ACTIVE",
                now,
                now
        );
    }

    public static User adminUser(String passwordHash) {
        Instant now = Instant.parse("2026-03-18T00:00:00Z");
        return new User(
                "user-admin",
                DEFAULT_TENANT_ID,
                List.of(DEFAULT_BRANCH_ID),
                "admin",
                "admin@lombardio.local",
                passwordHash,
                "System Admin",
                "ACTIVE",
                List.of(ADMIN_ROLE.id()),
                now,
                now
        );
    }

    public static User platformAdminUser(String passwordHash) {
        Instant now = Instant.parse("2026-03-18T00:00:00Z");
        return new User(
                "user-platform-admin",
                PLATFORM_TENANT_ID,
                List.of(),
                "platform-admin",
                "platform@lombardio.local",
                passwordHash,
                "Platform Admin",
                "ACTIVE",
                List.of(PLATFORM_ADMIN_ROLE.id()),
                now,
                now
        );
    }
}
