--liquibase formatted sql

--changeset dreamscale:2
create table task_switch_event (
  id uuid primary key not null,
  position timestamp,
  description text,
  project_id uuid,
  task_id uuid,
  organization_id uuid,
  member_id uuid
);