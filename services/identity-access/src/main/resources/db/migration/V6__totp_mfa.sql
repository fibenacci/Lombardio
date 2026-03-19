create table user_totp_credentials (
    user_id varchar(64) primary key,
    secret_ciphertext varchar(512) not null,
    enabled boolean not null,
    created_at timestamp with time zone not null,
    activated_at timestamp with time zone null,
    constraint fk_user_totp_credentials_user foreign key (user_id) references users (id) on delete cascade
);

create table mfa_challenges (
    id varchar(64) primary key,
    user_id varchar(64) not null,
    tenant_id varchar(64) not null,
    factor_type varchar(32) not null,
    created_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    constraint fk_mfa_challenges_user foreign key (user_id) references users (id) on delete cascade
);

create index idx_mfa_challenges_user_id on mfa_challenges (user_id);
create index idx_mfa_challenges_expires_at on mfa_challenges (expires_at);
