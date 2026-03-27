create table roles (
    id bigserial primary key,
    name varchar(100) not null unique
);

create table permissions (
    id bigserial primary key,
    name varchar(100) not null unique
);

create table role_permissions (
    role_id bigint not null,
    permission_id bigint not null,
    primary key (role_id, permission_id),
    constraint fk_role_permissions_role foreign key (role_id) references roles (id) on delete cascade,
    constraint fk_role_permissions_permission foreign key (permission_id) references permissions (id) on delete cascade
);
