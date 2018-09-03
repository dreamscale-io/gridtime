--liquibase formatted sql

--changeset dreamscale:0
create table config_project_sync (
  id uuid primary key not null,
  organization_id uuid,
  project_external_id text
);