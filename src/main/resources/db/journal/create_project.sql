--liquibase formatted sql

--changeset project:0
create table project (
  id uuid primary key not null,
  name text,
  external_id text
);