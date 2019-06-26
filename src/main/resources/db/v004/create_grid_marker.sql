--liquibase formatted sql

--changeset dreamscale:4
create table grid_marker (
  id uuid primary key not null,
  torchie_id uuid,
  tile_seq bigint,
  position timestamp,
  row_name text,
  tag_type text,
  tag_name text,
  grid_feature_id uuid
);