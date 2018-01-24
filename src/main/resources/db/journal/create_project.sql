--liquibase formatted sql

--changeset dreamscale:0
create table project (
  id uuid primary key not null,
  name text,
  external_id text
);