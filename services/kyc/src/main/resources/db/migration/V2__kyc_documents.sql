alter table kyc.kyc_records
    add column if not exists document_number varchar(128);

alter table kyc.kyc_records
    add column if not exists document_valid_until date;

alter table kyc.kyc_records
    add column if not exists document_front_image_data_url text;

alter table kyc.kyc_records
    add column if not exists document_back_image_data_url text;
