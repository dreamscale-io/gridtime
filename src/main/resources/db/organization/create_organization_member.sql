--liquibase formatted sql

--changeset dreamscale:0
create table organization_member (
  id uuid primary key not null,
  master_account_id uuid,
  email text,
  external_id text,
  organization_id uuid
);

