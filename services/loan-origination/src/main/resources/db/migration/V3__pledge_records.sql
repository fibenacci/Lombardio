alter table loan_origination.loan_cases
    add column if not exists customer_birth_date date;

alter table loan_origination.loan_cases
    add column if not exists customer_street varchar(255);

alter table loan_origination.loan_cases
    add column if not exists customer_postal_code varchar(32);

alter table loan_origination.loan_cases
    add column if not exists customer_city varchar(120);

alter table loan_origination.loan_cases
    add column if not exists customer_checked_document_type varchar(120);

create table if not exists loan_origination.pledge_records (
    id varchar(64) primary key,
    loan_case_id varchar(64) not null,
    tenant_id varchar(64) not null,
    recorded_at timestamp with time zone not null,
    language_code varchar(8) not null,
    retention_until date not null,
    pledgor_name varchar(160) not null,
    pledgor_street varchar(255),
    pledgor_postal_code varchar(32),
    pledgor_city varchar(120),
    pledgor_birth_date date,
    checked_document_type varchar(120),
    power_of_attorney_required boolean not null,
    bearer_name varchar(160),
    bearer_street varchar(255),
    bearer_postal_code varchar(32),
    bearer_city varchar(120),
    power_of_attorney_document_data_url text,
    sort_order integer not null default 0,
    constraint fk_pledge_records_case foreign key (loan_case_id) references loan_origination.loan_cases (id) on delete cascade
);

create index if not exists idx_pledge_records_tenant_recorded_at
    on loan_origination.pledge_records (tenant_id, recorded_at);
