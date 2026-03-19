create table tenants (
    id varchar(64) primary key,
    tenant_key varchar(100) not null unique,
    display_name varchar(120) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table tenant_features (
    tenant_id varchar(64) not null,
    feature_key varchar(100) not null,
    enabled boolean not null,
    updated_at timestamp with time zone not null,
    primary key (tenant_id, feature_key),
    constraint fk_tenant_features_tenant foreign key (tenant_id) references tenants (id) on delete cascade
);
