alter table customers
    add column if not exists birth_date date;

update customers
set birth_date = coalesce(birth_date, date '1980-01-01');

alter table customers
    alter column birth_date set not null;
