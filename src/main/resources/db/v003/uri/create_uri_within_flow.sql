--liquibase formatted sql

--changeset dreamscale:3
create table uri_within_flow (
  id uuid primary key not null,
  owner_id uuid,
  object_type text,
  uri text,
  relativePath text
);