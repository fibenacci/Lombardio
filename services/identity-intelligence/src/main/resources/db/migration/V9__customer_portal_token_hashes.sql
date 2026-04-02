alter table customer_portal_invitations
    add column if not exists token_hash varchar(64);

create unique index if not exists idx_customer_portal_invitations_token_hash_unique
    on customer_portal_invitations (token_hash);

alter table customer_portal_sessions
    add column if not exists token_hash varchar(64);

create unique index if not exists idx_customer_portal_sessions_token_hash_unique
    on customer_portal_sessions (token_hash);
