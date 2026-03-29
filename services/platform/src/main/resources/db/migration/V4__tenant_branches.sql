create table tenant_branches (
    id varchar(64) primary key,
    tenant_id varchar(64) not null,
    branch_key varchar(100) not null,
    display_name varchar(120) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_tenant_branches_tenant foreign key (tenant_id) references tenants (id) on delete cascade,
    constraint uk_tenant_branches_key unique (tenant_id, branch_key)
);
