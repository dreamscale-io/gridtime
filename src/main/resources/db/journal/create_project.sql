--liquibase formatted sql

--changeset dreamscale:0
create table project (
  id uuid primary key not null,
  name text,
  organization_id uuid,
  external_id text
);