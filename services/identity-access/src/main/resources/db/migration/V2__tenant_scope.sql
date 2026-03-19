alter table roles add column if not exists tenant_id varchar(64);
alter table users add column if not exists tenant_id varchar(64);

update roles set tenant_id = 'tenant-default' where tenant_id is null;
update users set tenant_id = 'tenant-default' where tenant_id is null;

alter table roles alter column tenant_id set not null;
alter table users alter column tenant_id set not null;

drop index if exists idx_roles_key_unique;
drop index if exists idx_users_email_unique;
drop index if exists idx_users_username_unique;

alter table roles drop constraint if exists roles_role_key_key;
alter table users drop constraint if exists users_username_key;
alter table users drop constraint if exists users_email_key;

create unique index if not exists idx_roles_tenant_key_unique on roles (tenant_id, role_key);
create unique index if not exists idx_users_tenant_username_unique on users (tenant_id, username);
create unique index if not exists idx_users_tenant_email_unique on users (tenant_id, email);
