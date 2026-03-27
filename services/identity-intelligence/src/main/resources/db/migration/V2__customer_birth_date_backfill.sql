alter table customer.customers
    add column if not exists birth_date date;

update customer.customers
set birth_date = coalesce(birth_date, date '1980-01-01');

alter table customer.customers
    alter column birth_date set not null;
