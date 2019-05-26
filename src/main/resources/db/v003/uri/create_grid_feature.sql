--liquibase formatted sql

--changeset dreamscale:3
create table grid_feature (
  id uuid primary key not null,
  team_id uuid,
  object_type text,
  search_key text,
  json text,
  unique (team_id, search_key)
);