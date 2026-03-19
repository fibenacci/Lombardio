create table pawn_tickets (
    id varchar(100) primary key,
    tenant_id varchar(100) not null,
    customer_id varchar(100) not null,
    customer_number varchar(100) not null,
    customer_display_name varchar(255) not null,
    customer_phone varchar(100),
    ticket_number varchar(100) not null unique,
    created_at timestamp with time zone not null,
    due_date date not null,
    earliest_auction_date date not null,
    term_months integer not null,
    loan_amount numeric(19,2) not null,
    monthly_interest_rate numeric(10,2) not null,
    monthly_operating_fee numeric(19,2) not null,
    manual_monthly_operating_fee_required boolean not null,
    total_interest_amount numeric(19,2) not null,
    total_operating_fee_amount numeric(19,2) not null,
    total_repayment_amount numeric(19,2) not null,
    legal_text text not null
);

create table pawn_ticket_positions (
    id varchar(120) primary key,
    pawn_ticket_id varchar(100) not null,
    sort_order integer not null,
    label varchar(255) not null,
    description varchar(255) not null,
    pledged_value numeric(19,2) not null,
    constraint fk_pawn_ticket_positions_ticket foreign key (pawn_ticket_id) references pawn_tickets (id) on delete cascade
);

create index idx_pawn_ticket_positions_ticket on pawn_ticket_positions (pawn_ticket_id, sort_order);
create index idx_pawn_tickets_tenant on pawn_tickets (tenant_id, created_at desc);

create table cash_transactions (
    id varchar(100) primary key,
    tenant_id varchar(100) not null,
    ticket_number varchar(100) not null,
    customer_number varchar(100) not null,
    customer_display_name varchar(255) not null,
    type varchar(50) not null,
    outstanding_loan_amount numeric(19,2) not null,
    interest_amount numeric(19,2) not null,
    operating_fee_amount numeric(19,2) not null,
    total_amount numeric(19,2) not null,
    legal_text text not null,
    note varchar(500),
    created_at timestamp with time zone not null
);

create index idx_cash_transactions_tenant on cash_transactions (tenant_id, created_at desc);
