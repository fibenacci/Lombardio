create table if not exists customer_portal_credentials (
    customer_id varchar(64) primary key,
    password_hash varchar(255) not null,
    activated_at timestamp with time zone not null,
    constraint fk_customer_portal_credentials_customer
        foreign key (customer_id) references customers (id) on delete cascade
);

create table if not exists customer_portal_invitations (
    token varchar(120) primary key,
    customer_id varchar(64) not null,
    tenant_id varchar(64) not null,
    email varchar(255) not null,
    issued_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    used_at timestamp with time zone,
    constraint fk_customer_portal_invitations_customer
        foreign key (customer_id) references customers (id) on delete cascade
);

create index if not exists idx_customer_portal_invitations_customer
    on customer_portal_invitations (customer_id, issued_at desc);

create table if not exists customer_portal_sessions (
    token varchar(120) primary key,
    customer_id varchar(64) not null,
    tenant_id varchar(64) not null,
    issued_at timestamp with time zone not null,
    constraint fk_customer_portal_sessions_customer
        foreign key (customer_id) references customers (id) on delete cascade
);

create index if not exists idx_customer_portal_sessions_customer
    on customer_portal_sessions (customer_id, issued_at desc);
