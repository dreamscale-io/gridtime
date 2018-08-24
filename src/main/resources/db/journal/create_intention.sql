--liquibase formatted sql

--changeset dreamscale:0
create table intention (
  id uuid primary key not null,
  position timestamp,
  description text,
  project_id uuid,
  task_id uuid,
  organization_id uuid,
  member_id uuid
);