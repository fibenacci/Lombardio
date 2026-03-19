alter table session_tokens add column if not exists actor_user_id varchar(64);
alter table session_tokens add column if not exists tenant_id varchar(64);

update session_tokens
set actor_user_id = user_id
where actor_user_id is null;

update session_tokens st
set tenant_id = u.tenant_id
from users u
where st.user_id = u.id
  and st.tenant_id is null;

alter table session_tokens alter column actor_user_id set not null;
alter table session_tokens alter column tenant_id set not null;

alter table session_tokens
    add constraint fk_session_tokens_actor_user
    foreign key (actor_user_id) references users (id) on delete cascade;

create index if not exists idx_session_tokens_actor_user_id on session_tokens (actor_user_id);
create index if not exists idx_session_tokens_tenant_id on session_tokens (tenant_id);
