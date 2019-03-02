--liquibase formatted sql

--changeset dreamscale:2
create table circle_context (
  id uuid primary key not null,
  position timestamp,
  description text,
  project_id uuid,
  task_id uuid,
  organization_id uuid,
  member_id uuid,
  flame_rating integer,
  finish_status text,
  finish_time timestamp,
  circle_id uuid
);