--liquibase formatted sql

--changeset dreamscale:4
create table grid_row (
  id uuid primary key not null,
  torchie_id uuid not null,
  zoom_level text,
  tile_seq bigint,
  row_name text,
  json text
);