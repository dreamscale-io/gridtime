--liquibase formatted sql

--changeset dreamscale:4
create table grid_tile_summary (
  id uuid primary key not null,
  torchie_id uuid,
  zoom_level text,
  tile_seq bigint,
  time_in_tile int,
  time_in_wtf int,
  time_in_learning int,
  time_in_progress int,
  time_in_pairing int,
  avg_flame float,
  avg_batch_size float,
  avg_traversal_speed float,
  avg_execution_time float,
  avg_red_to_green_time float
);