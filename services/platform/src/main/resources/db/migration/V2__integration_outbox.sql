create table integration_outbox_events (
    id varchar(128) primary key,
    aggregate_type varchar(64) not null,
    aggregate_id varchar(128) not null,
    event_type varchar(128) not null,
    tenant_id varchar(128) not null,
    payload text not null,
    status varchar(32) not null,
    attempt_count integer not null default 0,
    occurred_at timestamp with time zone not null,
    next_attempt_at timestamp with time zone not null,
    locked_at timestamp with time zone,
    locked_by varchar(128),
    published_at timestamp with time zone,
    last_error text
);

create index idx_integration_outbox_claim
    on integration_outbox_events (status, next_attempt_at, occurred_at);
