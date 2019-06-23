--liquibase formatted sql

--changeset dreamscale:4
create table grid_marker (
  id uuid primary key not null,
  torchie_id uuid,
  row_name text,
  tile_seq bigint,
  beat_number int,
  start_or_stop text,
  grid_feature_id uuid
);