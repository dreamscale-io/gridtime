--liquibase formatted sql

--changeset dreamscale:4
create table grid_bridge_metrics (
  id uuid primary key not null,
  team_id uuid not null,
  torchie_id uuid not null,
  zoom_level text,
  tile_seq bigint,
  bridge_feature_id uuid,
  total_visits int,
  visits_during_wtf int,
  visits_during_learning int,
  visits_during_red_to_green int,
  avg_flame float,
  avg_traversal_speed float
);