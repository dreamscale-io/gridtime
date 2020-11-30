--liquibase formatted sql

--changeset dreamscale:5
alter table grid_time_calendar rename to grid_calendar;

alter table grid_calendar
rename column clock_time to start_time;

alter table grid_calendar
add column end_time timestamp;


alter table grid_calendar drop constraint grid_time_ux;

alter table grid_calendar add constraint grid_time_ux unique (zoom_level, start_time);

drop view zoomable_idea_flow_metrics_v;

create view zoomable_idea_flow_metrics_v as
  select i.id, c.zoom_level, c.tile_seq, c.grid_time, c.start_time, c.end_time, i.torchie_id, i.time_in_tile,
         i.avg_flame, i.percent_wtf, i.percent_learning, i.percent_progress, i.percent_pairing
  from grid_idea_flow_metrics i, grid_calendar c
  where i.zoom_level = c.zoom_level and i.tile_seq = c.tile_seq;
;

drop view zoomable_box_metrics_v;

create view zoomable_box_metrics_v as
  select b.id, c.zoom_level, c.tile_seq, c.grid_time, c.start_time, c.end_time, b.torchie_id, b.time_in_box, b.box_feature_id, f.search_key as box_uri,
         b.avg_flame, b.percent_wtf, b.percent_learning, b.percent_progress, b.percent_pairing,
         b.avg_file_batch_size, b.avg_traversal_speed, b.avg_execution_time
  from grid_box_metrics b, grid_calendar c, grid_feature f
  where b.zoom_level = c.zoom_level and b.tile_seq = c.tile_seq and b.box_feature_id = f.id;
;

drop view zoomable_team_box_metrics_v;

create view zoomable_team_box_metrics_v as
  select b.id, tm.team_id, mn.full_name as member_name, c.zoom_level, c.tile_seq, c.grid_time, c.start_time, c.end_time,
         b.torchie_id, b.time_in_box, b.box_feature_id, f.search_key as box_uri,
         b.avg_flame, b.percent_wtf, b.percent_learning, b.percent_progress, b.percent_pairing,
         b.avg_file_batch_size, b.avg_traversal_speed, b.avg_execution_time
  from team_member tm, member_name_view mn, grid_box_metrics b, grid_calendar c, grid_feature f
  where b.zoom_level = c.zoom_level and b.tile_seq = c.tile_seq and b.box_feature_id = f.id
  and tm.member_id = b.torchie_id and b.torchie_id = mn.torchie_id;


drop view zoomable_team_idea_flow_metrics_v;

create view zoomable_team_idea_flow_metrics_v as
  select tm.team_id, mn.full_name as member_name, ifm.id, gtc.zoom_level, gtc.tile_seq, gtc.grid_time, gtc.start_time, gtc.end_time, ifm.torchie_id,
         ifm.time_in_tile, ifm.avg_flame, ifm.percent_wtf, ifm.percent_learning, ifm.percent_progress, ifm.percent_pairing
  from team_member tm, member_name_view mn, grid_idea_flow_metrics ifm, grid_calendar gtc
  where ifm.zoom_level = gtc.zoom_level and ifm.tile_seq = gtc.tile_seq
  and tm.member_id = ifm.torchie_id and ifm.torchie_id = mn.torchie_id;
;
