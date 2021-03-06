--liquibase formatted sql

--changeset dreamscale:4

drop view zoomable_team_box_metrics_v;

create view zoomable_team_box_metrics_v as
  select b.id, tm.team_id, mn.full_name as member_name, c.zoom_level, c.tile_seq, c.grid_time, c.clock_time,
         b.torchie_id, b.time_in_box, b.box_feature_id, f.search_key as box_uri,
         b.avg_flame, b.percent_wtf, b.percent_learning, b.percent_progress, b.percent_pairing,
         b.avg_file_batch_size, b.avg_traversal_speed, b.avg_execution_time
  from team_member tm, member_name_view mn, grid_box_metrics b, grid_time_calendar c, grid_feature f
  where b.zoom_level = c.zoom_level and b.tile_seq = c.tile_seq and b.box_feature_id = f.id
  and tm.member_id = b.torchie_id and b.torchie_id = mn.torchie_id;