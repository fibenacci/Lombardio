alter table pawn_tickets
    add column if not exists contract_number varchar(100);

alter table pawn_tickets
    add column if not exists contract_barcode varchar(100);

alter table pawn_tickets
    add column if not exists terms_version varchar(50);

alter table pawn_tickets
    add column if not exists terms_and_conditions_text text;

update pawn_tickets
set contract_number = coalesce(contract_number, ticket_number),
    contract_barcode = coalesce(contract_barcode, ticket_number),
    terms_version = coalesce(terms_version, 'AGB-2026-03'),
    terms_and_conditions_text = coalesce(
        terms_and_conditions_text,
        'Geschaeftsbedingungen fuer den Pfandleihvertrag: Der Pfandschein ist Vertragsnachweis und bei Ausloesung vorzulegen.'
    );

alter table pawn_tickets
    alter column contract_number set not null;

alter table pawn_tickets
    alter column contract_barcode set not null;

alter table pawn_tickets
    alter column terms_version set not null;

alter table pawn_tickets
    alter column terms_and_conditions_text set not null;

create unique index if not exists idx_pawn_tickets_contract_number
    on pawn_tickets (contract_number);

alter table pawn_ticket_positions
    add column if not exists item_number varchar(120);

alter table pawn_ticket_positions
    add column if not exists item_barcode varchar(120);

update pawn_ticket_positions position
set item_number = coalesce(
        item_number,
        ticket.contract_number || '-' || lpad((position.sort_order + 1)::text, 2, '0')
    ),
    item_barcode = coalesce(
        item_barcode,
        ticket.contract_number || '-' || lpad((position.sort_order + 1)::text, 2, '0')
    )
from pawn_tickets ticket
where ticket.id = position.pawn_ticket_id;

alter table pawn_ticket_positions
    alter column item_number set not null;

alter table pawn_ticket_positions
    alter column item_barcode set not null;
