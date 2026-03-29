
create table if not exists valuation_guidelines (
    id varchar(64) primary key,
    tenant_id varchar(64) not null,
    category varchar(120) not null,
    material varchar(120) not null,
    label varchar(160) not null,
    description varchar(255) not null,
    base_loan_value numeric(19, 2) not null
);

create index if not exists idx_valuation_guidelines_tenant_id
    on valuation_guidelines (tenant_id);

create table if not exists loan_cases (
    id varchar(64) primary key,
    tenant_id varchar(64) not null,
    customer_id varchar(64) not null,
    customer_number varchar(64) not null,
    customer_display_name varchar(160) not null,
    customer_phone varchar(64),
    customer_kyc_status varchar(32),
    customer_kyc_approved boolean not null
);

create table if not exists loan_positions (
    id varchar(64) primary key,
    loan_case_id varchar(64) not null,
    ticket_group integer not null,
    label varchar(160) not null,
    description varchar(255) not null,
    guideline_id varchar(64) not null,
    guideline_label varchar(160) not null,
    base_loan_value numeric(19, 2) not null,
    pledged_value numeric(19, 2) not null,
    sort_order integer not null,
    constraint fk_loan_positions_case foreign key (loan_case_id) references loan_cases (id) on delete cascade
);

create table if not exists loan_pawn_tickets (
    id varchar(64) primary key,
    loan_case_id varchar(64) not null,
    ticket_number varchar(64) not null,
    created_at timestamp with time zone not null,
    due_date date not null,
    earliest_auction_date date not null,
    term_months integer not null,
    total_loan_value numeric(19, 2) not null,
    monthly_interest_rate numeric(19, 2) not null,
    monthly_operating_fee numeric(19, 2) not null,
    manual_monthly_operating_fee_required boolean not null,
    total_interest_amount numeric(19, 2) not null,
    total_operating_fee_amount numeric(19, 2) not null,
    total_repayment_amount numeric(19, 2) not null,
    legal_text text not null,
    sort_order integer not null,
    constraint fk_loan_pawn_tickets_case foreign key (loan_case_id) references loan_cases (id) on delete cascade
);
