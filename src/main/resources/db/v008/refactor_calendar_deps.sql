--liquibase formatted sql

--changeset dreamscale:5

drop view zoomable_box_metrics_v;

drop view zoomable_idea_flow_metrics_v;

drop view zoomable_team_box_metrics_v;

drop view zoomable_team_idea_flow_metrics_v;

drop view work_ready_by_team_view;




truncate table terminal_circuit_location_history;

alter table terminal_circuit_location_history drop column zoom_level;

alter table terminal_circuit_location_history drop column tile_seq;

alter table terminal_circuit_location_history add column calendar_id uuid not null;

truncate table grid_marker;

alter table grid_marker drop column tile_seq;

alter table grid_marker add column calendar_id uuid not null;

truncate table grid_row;

alter table grid_row drop column zoom_level;

alter table grid_row drop column tile_seq;

alter table grid_row add column calendar_id uuid not null;

truncate table grid_box_metrics;

alter table grid_box_metrics drop column zoom_level;

alter table grid_box_metrics drop column tile_seq;

alter table grid_box_metrics add column calendar_id uuid not null;

truncate table grid_idea_flow_metrics;

alter table grid_idea_flow_metrics drop column zoom_level;

alter table grid_idea_flow_metrics drop column tile_seq;

alter table grid_idea_flow_metrics add column calendar_id uuid not null;

truncate table work_item_to_aggregate;

alter table work_item_to_aggregate drop column zoom_level;

alter table work_item_to_aggregate drop column tile_seq;

alter table work_item_to_aggregate add column calendar_id uuid not null;




create view zoomable_box_metrics_v as
  select b.id, c.id calendar_id, c.zoom_level, c.tile_seq, c.grid_time, c.start_time, c.end_time, b.torchie_id, b.time_in_box, b.box_feature_id, f.search_key as box_uri,
         b.avg_flame, b.percent_wtf, b.percent_learning, b.percent_progress, b.percent_pairing,
         b.avg_file_batch_size, b.avg_traversal_speed, b.avg_execution_time
  from grid_box_metrics b, grid_calendar c, grid_feature f
  where b.calendar_id = c.id and b.box_feature_id = f.id;
;



create view zoomable_idea_flow_metrics_v as
  select i.id, c.id calendar_id, c.zoom_level, c.tile_seq, c.grid_time, c.start_time, c.end_time, i.torchie_id, i.time_in_tile,
         i.avg_flame, i.percent_wtf, i.percent_learning, i.percent_progress, i.percent_pairing
  from grid_idea_flow_metrics i, grid_calendar c
  where i.calendar_id = c.id;
;



create view zoomable_team_box_metrics_v as
  select b.id, tm.team_id, mn.full_name as member_name, c.id calendar_id, c.zoom_level, c.tile_seq, c.grid_time, c.start_time, c.end_time,
         b.torchie_id, b.time_in_box, b.box_feature_id, f.search_key as box_uri,
         b.avg_flame, b.percent_wtf, b.percent_learning, b.percent_progress, b.percent_pairing,
         b.avg_file_batch_size, b.avg_traversal_speed, b.avg_execution_time
  from team_member tm, member_name_view mn, grid_box_metrics b, grid_calendar c, grid_feature f
  where b.calendar_id = c.id and b.box_feature_id = f.id
  and tm.member_id = b.torchie_id and b.torchie_id = mn.torchie_id;




create view zoomable_team_idea_flow_metrics_v as
  select tm.team_id, mn.full_name as member_name, ifm.id, gtc.id calendar_id, gtc.zoom_level, gtc.tile_seq, gtc.grid_time, gtc.start_time, gtc.end_time, ifm.torchie_id,
         ifm.time_in_tile, ifm.avg_flame, ifm.percent_wtf, ifm.percent_learning, ifm.percent_progress, ifm.percent_pairing
  from team_member tm, member_name_view mn, grid_idea_flow_metrics ifm, grid_calendar gtc
  where ifm.calendar_id = gtc.id
  and tm.member_id = ifm.torchie_id and ifm.torchie_id = mn.torchie_id;
;



create view work_ready_by_team_view as
  select wi.team_id, wi.calendar_id, wi.grid_time, wi.work_to_do_type,
         min(event_time) as earliest_event_time, count(*) as tile_count,
         (SELECT count(tm.member_id)
          FROM team_member tm, torchie_feed_cursor f
          WHERE wi.team_id = tm.team_id and tm.member_id = f.torchie_id ) as team_size
  from work_item_to_aggregate wi
  where wi.processing_state = 'Ready'
  group by wi.team_id, wi.calendar_id, wi.grid_time, wi.work_to_do_type
  order by earliest_event_time;
;
