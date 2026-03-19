create schema if not exists online_auction;

create table if not exists online_auction.online_auctions (
    id varchar(120) primary key,
    tenant_id varchar(120) not null,
    title varchar(255) not null,
    slug varchar(160) not null,
    status varchar(40) not null,
    channel_name varchar(255) not null,
    published_at timestamp with time zone,
    live_started_at timestamp with time zone,
    closed_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table if not exists online_auction.online_auction_lots (
    id varchar(120) primary key,
    auction_id varchar(120) not null references online_auction.online_auctions(id) on delete cascade,
    lot_number integer not null,
    title varchar(255) not null,
    description varchar(1000) not null,
    starting_bid numeric(19, 2) not null,
    current_bid numeric(19, 2) not null,
    highest_bidder_alias varchar(255)
);

create table if not exists online_auction.bidder_registrations (
    id varchar(120) primary key,
    auction_id varchar(120) not null references online_auction.online_auctions(id) on delete cascade,
    display_name varchar(255) not null,
    email varchar(255) not null,
    paddle_number varchar(60) not null,
    access_token varchar(160) not null,
    created_at timestamp with time zone not null
);

create unique index if not exists idx_online_auction_slug
    on online_auction.online_auctions (tenant_id, slug);

create unique index if not exists idx_online_auction_lot_number
    on online_auction.online_auction_lots (auction_id, lot_number);

create unique index if not exists idx_bidder_registration_token
    on online_auction.bidder_registrations (access_token);
