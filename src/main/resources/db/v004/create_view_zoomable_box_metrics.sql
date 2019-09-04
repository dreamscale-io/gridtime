--liquibase formatted sql

--changeset dreamscale:4

create view zoomable_box_metrics_v as
  select c.zoom_level, c.tile_seq, c.grid_time, c.clock_time, b.torchie_id, b.time_in_box, b.box_feature_id,
         b.avg_flame, b.percent_wtf, b.percent_learning, b.percent_progress, b.percent_pairing,
         b.avg_file_batch_size, b.avg_traversal_speed, b.avg_execution_time
  from grid_box_metrics b, grid_time_calendar c
  where b.zoom_level = c.zoom_level and b.tile_seq = c.tile_seq;
;


-- id uuid primary key not null,
-- torchie_id uuid,
-- zoom_level text,
-- tile_seq bigint,
-- box_feature_id uuid,
-- time_in_box bigint,
-- avg_flame double precision,
-- percent_wtf double precision,
-- percent_learning double precision,
-- percent_progress double precision,
-- percent_pairing double precision,
--
-- avg_file_batch_size double precision,
-- avg_traversal_speed double precision,
-- avg_execution_time double precision