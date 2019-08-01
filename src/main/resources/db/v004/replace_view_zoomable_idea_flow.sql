--liquibase formatted sql

--changeset dreamscale:4
drop view grid_idea_flow_metrics_v;

create view zoomable_idea_flow_metrics_v as
  select i.id, c.zoom_level, c.tile_seq, c.grid_time, c.clock_time, i.torchie_id, i.time_in_tile,
         i.avg_flame, i.percent_wtf, i.percent_learning, i.percent_progress, i.percent_pairing
  from grid_idea_flow_metrics i, grid_time_calendar c
  where i.zoom_level = c.zoom_level and i.tile_seq = c.tile_seq;
;
