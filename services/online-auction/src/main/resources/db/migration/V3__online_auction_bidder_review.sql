alter table bidder_registrations
    add column if not exists legal_name varchar(255) not null default '',
    add column if not exists birth_date varchar(40) not null default '',
    add column if not exists iban varchar(80) not null default '',
    add column if not exists kyc_status varchar(40) not null default 'PENDING',
    add column if not exists account_check_status varchar(40) not null default 'PENDING',
    add column if not exists review_note varchar(1000);
