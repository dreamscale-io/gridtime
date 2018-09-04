--liquibase formatted sql

--changeset dreamscale:0
create table task (
  id uuid primary key not null,
  name text,
  summary text,
  status text,
  external_id text,
  project_id uuid,
  organization_id uuid
);