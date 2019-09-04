--liquibase formatted sql

--changeset dreamscale:4

drop table grid_box_metrics;

create table grid_box_metrics (
  id uuid primary key not null,
  torchie_id uuid,
  zoom_level text,
  tile_seq bigint,
  box_feature_id uuid,
  time_in_box bigint,
  avg_flame double precision,
  percent_wtf double precision,
  percent_learning double precision,
  percent_progress double precision,
  percent_pairing double precision,

  avg_file_batch_size double precision,
  avg_traversal_speed double precision,
  avg_execution_time double precision
);

