--liquibase formatted sql

--changeset dreamscale:3
create table story_grid_summary (
  id uuid primary key not null,
  torchie_id uuid,
  tile_id uuid,

  average_mood double precision,
  percent_learning double precision,
  percent_troubleshooting double precision,
  percent_progress double precision,
  percent_pairing double precision,

  boxes_visited integer,
  locations_visited integer,
  traversals_visited integer,
  bridges_visited integer,
  bubbles_visited integer,

  total_experiments integer,
  total_messages integer
);

