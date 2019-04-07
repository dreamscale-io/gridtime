--liquibase formatted sql

--changeset dreamscale:3
create table uri_within_project (
  id uuid primary key not null,
  project_id uuid,
  object_type text,
  object_key text,
  uri text,
  relativePath text
);