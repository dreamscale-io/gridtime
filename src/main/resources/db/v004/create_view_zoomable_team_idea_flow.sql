--liquibase formatted sql

--changeset dreamscale:4

create view zoomable_team_idea_flow_metrics_v as
  select tm.team_id, mn.full_name as member_name, ifm.id, gtc.zoom_level, gtc.tile_seq, gtc.grid_time, gtc.clock_time, ifm.torchie_id,
         ifm.time_in_tile, ifm.avg_flame, ifm.percent_wtf, ifm.percent_learning, ifm.percent_progress, ifm.percent_pairing
  from team_member tm, member_name_view mn, grid_idea_flow_metrics ifm, grid_time_calendar gtc
  where ifm.zoom_level = gtc.zoom_level and ifm.tile_seq = gtc.tile_seq
  and tm.member_id = ifm.torchie_id and ifm.torchie_id = mn.torchie_id;
;
