create table if not exists user_branches (
    user_id varchar(64) not null,
    branch_id varchar(64) not null,
    primary key (user_id, branch_id),
    constraint fk_user_branches_user foreign key (user_id) references users (id) on delete cascade,
    constraint fk_user_branches_branch foreign key (branch_id) references branches (id) on delete cascade
);

create index if not exists idx_user_branches_branch_id on user_branches (branch_id);
