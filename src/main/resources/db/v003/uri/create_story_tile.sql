--liquibase formatted sql

--changeset dreamscale:3
create table story_tile (
  id uuid primary key not null,
  torchie_id uuid,
  uri text,
  zoom_level text,
  clock_position timestamp,
  year integer,
  block integer,
  weeks_into_block integer,
  weeks_into_year integer,
  days_into_week integer,
  four_hour_steps integer,
  twenty_minute_steps integer,
  json_tile text
);
