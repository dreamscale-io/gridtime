--liquibase formatted sql

--changeset dreamscale:0
create table event (
  id uuid primary key not null,
  position timestamp,
  description text,
  project_id uuid,
  task_id uuid
);