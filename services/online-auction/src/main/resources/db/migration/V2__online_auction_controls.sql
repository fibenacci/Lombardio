alter table online_auctions
    add column minimum_increment numeric(19, 2) not null default 1.00,
    add column countdown_seconds integer not null default 180,
    add column countdown_ends_at timestamp with time zone;

alter table bidder_registrations
    add column approval_status varchar(40) not null default 'PENDING',
    add column approved_at timestamp with time zone;
