create table aml_cases (
    id varchar(100) primary key,
    tenant_id varchar(100) not null,
    customer_id varchar(100) not null,
    status varchar(50) not null,
    risk_level varchar(50) not null,
    pep_flag boolean not null,
    sanctions_hit boolean not null,
    unusual_transaction_flag boolean not null,
    source_of_funds_checked boolean not null,
    suspicious_activity_reported boolean not null,
    goaml_reference varchar(100),
    decision_note varchar(1000),
    last_screened_at timestamp with time zone,
    reviewed_at timestamp with time zone,
    updated_at timestamp with time zone not null
);

create unique index idx_aml_cases_customer on aml_cases (tenant_id, customer_id);
