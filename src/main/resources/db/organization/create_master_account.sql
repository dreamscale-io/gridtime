--liquibase formatted sql

--changeset dreamscale:0
create table master_account (
  id uuid primary key not null,
  master_email text,
  full_name text,
  activation_code text,
  activation_date timestamp,
  api_key text
);
