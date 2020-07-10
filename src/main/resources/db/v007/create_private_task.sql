--liquibase formatted sql

--changeset dreamscale:5

create table private_task (
  id uuid primary key not null,
  name text,
  lowercase_name text,
  description text,
  project_id uuid,
  organization_id uuid,
  member_id uuid
);

alter table private_task
add constraint private_task_name_unique_key unique (project_id, member_id, name);

