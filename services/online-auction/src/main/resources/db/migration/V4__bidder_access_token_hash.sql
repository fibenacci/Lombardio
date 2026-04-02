alter table bidder_registrations
    add column if not exists access_token_hash varchar(64);

create unique index if not exists idx_bidder_registration_token_hash
    on bidder_registrations (access_token_hash)
    where access_token_hash is not null;
