--liquibase formatted sql

--changeset dreamscale:0
create table active_work_status (
  id uuid primary key not null,
  organization_id uuid not null,
  member_id uuid not null,
  active_task_id uuid,
  working_on text,
  last_update timestamp
);
