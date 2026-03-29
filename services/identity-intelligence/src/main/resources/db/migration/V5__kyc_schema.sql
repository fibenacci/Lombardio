
create table if not exists kyc_records (
    id varchar(64) primary key,
    tenant_id varchar(64) not null,
    customer_id varchar(64) not null,
    verification_mode varchar(32) not null,
    status varchar(32) not null,
    verified_until date,
    document_type varchar(120),
    decision_note varchar(255),
    provider_name varchar(120),
    provider_reference varchar(120),
    provider_status varchar(64)
);

create unique index if not exists idx_kyc_records_tenant_customer_unique
    on kyc_records (tenant_id, customer_id);
