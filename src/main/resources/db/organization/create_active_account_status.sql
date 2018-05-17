--liquibase formatted sql

--changeset dreamscale:0
create table active_account_status (
  master_account_id uuid primary key not null,
  connection_id uuid,
  last_activity timestamp,
  delta_time int,
  active_status text
);
