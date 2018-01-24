--liquibase formatted sql

--changeset project:0
create table task (
  id uuid primary key not null,
  name text,
  summary text,
  external_id text,
  project_id uuid
);