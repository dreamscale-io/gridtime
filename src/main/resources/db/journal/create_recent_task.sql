--liquibase formatted sql

--changeset dreamscale:0
create table recent_task (
  id uuid primary key not null,
  task_id uuid not null,
  project_id uuid,
  member_id uuid,
  organization_id uuid,
  last_accessed TIMESTAMP
);