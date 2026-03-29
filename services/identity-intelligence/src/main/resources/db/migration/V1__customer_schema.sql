
create table if not exists customers (
    id varchar(64) primary key,
    tenant_id varchar(64) not null,
    customer_number varchar(64) not null,
    first_name varchar(120) not null,
    last_name varchar(120) not null,
    birth_date date not null,
    phone varchar(64) not null,
    email varchar(255),
    wants_digital_pawn_ticket boolean not null default false,
    online_access_status varchar(32) not null default 'NOT_REQUESTED',
    street varchar(255),
    postal_code varchar(32),
    city varchar(120)
);

create unique index if not exists idx_customers_tenant_customer_number_unique
    on customers (tenant_id, customer_number);
create index if not exists idx_customers_tenant_id on customers (tenant_id);
