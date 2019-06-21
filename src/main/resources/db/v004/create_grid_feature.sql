--liquibase formatted sql

--changeset dreamscale:4
create table grid_feature (
  id uuid primary key not null,
  team_id uuid,
  type_uri text,
  search_key text,
  json text,
  unique (team_id, search_key)
);