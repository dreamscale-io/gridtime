--liquibase formatted sql

--changeset dreamscale:0
create table master_acccount (
  id uuid primary key not null,
  master_email text,
  full_name text,
  activation_code text,
  activation_date timestamp
);
