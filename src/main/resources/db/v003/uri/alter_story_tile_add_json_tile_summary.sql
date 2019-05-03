--liquibase formatted sql

--changeset dreamscale:3
alter table story_tile
  add column json_tile_summary text;

alter table story_tile
  add column dream_time text;