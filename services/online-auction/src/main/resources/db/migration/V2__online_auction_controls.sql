alter table online_auctions
    add column if not exists minimum_increment numeric(19, 2) not null default 1.00,
    add column if not exists countdown_seconds integer not null default 180,
    add column if not exists countdown_ends_at timestamp with time zone;

alter table bidder_registrations
    add column if not exists approval_status varchar(40) not null default 'PENDING',
    add column if not exists approved_at timestamp with time zone;
