create table if not exists branches (
    id varchar(64) primary key,
    tenant_id varchar(64) not null,
    branch_key varchar(100) not null,
    display_name varchar(120) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index if not exists idx_branches_tenant_key_unique on branches (tenant_id, branch_key);
create index if not exists idx_branches_tenant_id on branches (tenant_id);
