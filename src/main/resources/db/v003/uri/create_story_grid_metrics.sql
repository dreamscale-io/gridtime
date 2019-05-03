--liquibase formatted sql

--changeset dreamscale:3
create table story_grid_metrics (
  id uuid primary key not null,
  torchie_id uuid,
  tile_id uuid,
  feature_uri text,
  candle_type text,
  sample_count integer,
  total double precision,
  avg double precision,
  stddev double precision,
  min double precision,
  max double precision
);

