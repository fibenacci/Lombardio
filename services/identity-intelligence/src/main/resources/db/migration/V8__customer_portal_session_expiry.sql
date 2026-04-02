alter table customer_portal_sessions
    add column if not exists expires_at timestamp with time zone;

update customer_portal_sessions
set expires_at = issued_at + INTERVAL '30' DAY
where expires_at is null;

alter table customer_portal_sessions
    alter column expires_at set not null;
