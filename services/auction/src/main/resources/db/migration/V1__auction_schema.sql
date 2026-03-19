create table if not exists auction.auctions (
    id varchar(120) primary key,
    tenant_id varchar(120) not null,
    title varchar(255) not null,
    location varchar(255) not null,
    status varchar(40) not null,
    public_announcement_date date,
    auction_date date,
    live_started_at timestamp with time zone,
    closed_at timestamp with time zone,
    announcement_reference varchar(255),
    realtime_channel varchar(255) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table if not exists auction.auction_lots (
    id varchar(120) primary key,
    auction_id varchar(120) not null references auction.auctions(id) on delete cascade,
    lot_number integer not null,
    contract_number varchar(120) not null,
    item_number varchar(120) not null,
    description varchar(1000) not null,
    estimated_value numeric(19, 2) not null,
    outstanding_claim numeric(19, 2) not null,
    latest_bid_amount numeric(19, 2) not null,
    leading_bidder varchar(255),
    hammer_price numeric(19, 2),
    status varchar(40) not null,
    surplus_amount numeric(19, 2),
    authority_transfer_due_date date,
    authority_transfer_status varchar(60)
);

create index if not exists idx_auction_auctions_tenant
    on auction.auctions (tenant_id, created_at desc);

create unique index if not exists idx_auction_lots_auction_number
    on auction.auction_lots (auction_id, lot_number);
