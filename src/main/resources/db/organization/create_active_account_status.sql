--liquibase formatted sql

--changeset dreamscale:0
create table active_account_status (
  master_account_id uuid primary key not null,
  last_activity timestamp,
  active_status text
);
