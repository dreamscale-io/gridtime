--liquibase formatted sql

--changeset dreamscale:4
create table grid_tile_idea_flow (
  id uuid primary key not null,
  torchie_id uuid,
  zoom_level text,
  tile_seq bigint,
  last_idea_flow_state uuid,
  time_in_tile bigint,
  avg_flame double precision,
  percent_wtf double precision,
  percent_learning double precision,
  percent_progress double precision,
  percent_pairing double precision
);

