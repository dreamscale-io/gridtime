--liquibase formatted sql

--changeset dreamscale:0
create table recent_project (
  id uuid primary key not null,
  project_id uuid not null,
  member_id uuid,
  organization_id uuid,
  last_accessed TIMESTAMP
);