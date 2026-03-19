create table permissions (
    permission_key varchar(100) primary key,
    display_name varchar(120) not null,
    description varchar(255) not null
);

create table roles (
    id varchar(64) primary key,
    role_key varchar(100) not null unique,
    display_name varchar(120) not null,
    description varchar(255) not null,
    active boolean not null
);

create table role_permissions (
    role_id varchar(64) not null,
    permission_key varchar(100) not null,
    primary key (role_id, permission_key),
    constraint fk_role_permissions_role foreign key (role_id) references roles (id) on delete cascade,
    constraint fk_role_permissions_permission foreign key (permission_key) references permissions (permission_key) on delete cascade
);

create table users (
    id varchar(64) primary key,
    username varchar(100) not null unique,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    display_name varchar(120) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table user_roles (
    user_id varchar(64) not null,
    role_id varchar(64) not null,
    primary key (user_id, role_id),
    constraint fk_user_roles_user foreign key (user_id) references users (id) on delete cascade,
    constraint fk_user_roles_role foreign key (role_id) references roles (id) on delete cascade
);

create table session_tokens (
    token varchar(128) primary key,
    user_id varchar(64) not null,
    issued_at timestamp with time zone not null,
    constraint fk_session_tokens_user foreign key (user_id) references users (id) on delete cascade
);

create index idx_session_tokens_user_id on session_tokens (user_id);
