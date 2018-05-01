--liquibase formatted sql

--changeset dreamscale:0
create table active_work_status (
  id uuid primary key not null,
  master_account_id uuid not null,
  organization_id uuid not null,
  last_update timestamp,
  work_status text,
  working_on text
);
