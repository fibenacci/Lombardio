create table operator_sessions (
    id varchar(128) primary key,
    access_token_ciphertext text not null,
    refresh_token_ciphertext text not null,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_operator_sessions_expires_at on operator_sessions (expires_at);
