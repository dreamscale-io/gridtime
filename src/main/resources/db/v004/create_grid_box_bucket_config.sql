--liquibase formatted sql

--changeset dreamscale:4
create table grid_box_bucket_config (
  id uuid primary key not null,
  team_id uuid,
  project_id uuid,
  box_name text,
  box_matcher_config_json text
);