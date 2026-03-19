alter table loan_origination.loan_pawn_tickets
    add column if not exists contract_number varchar(64);

alter table loan_origination.loan_pawn_tickets
    add column if not exists contract_barcode varchar(64);

alter table loan_origination.loan_pawn_tickets
    add column if not exists terms_version varchar(50);

alter table loan_origination.loan_pawn_tickets
    add column if not exists terms_and_conditions_text text;

update loan_origination.loan_pawn_tickets
set contract_number = coalesce(contract_number, ticket_number),
    contract_barcode = coalesce(contract_barcode, ticket_number),
    terms_version = coalesce(terms_version, 'AGB-2026-03'),
    terms_and_conditions_text = coalesce(
        terms_and_conditions_text,
        'Geschaeftsbedingungen fuer den Pfandleihvertrag: Der Pfandschein ist Vertragsnachweis und bei Ausloesung vorzulegen.'
    );

alter table loan_origination.loan_pawn_tickets
    alter column contract_number set not null;

alter table loan_origination.loan_pawn_tickets
    alter column contract_barcode set not null;

alter table loan_origination.loan_pawn_tickets
    alter column terms_version set not null;

alter table loan_origination.loan_pawn_tickets
    alter column terms_and_conditions_text set not null;
