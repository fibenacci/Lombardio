alter table customer.customers
    add column if not exists email varchar(255);

alter table customer.customers
    add column if not exists wants_digital_pawn_ticket boolean not null default false;

alter table customer.customers
    add column if not exists online_access_status varchar(32) not null default 'NOT_REQUESTED';

update customer.customers
set online_access_status = case
    when wants_digital_pawn_ticket then 'INVITED'
    else 'NOT_REQUESTED'
end
where online_access_status is null
   or online_access_status = '';
