--liquibase formatted sql

--changeset dreamscale:4
create view work_ready_by_team_view as
  select wi.team_id, wi.zoom_level, wi.tile_seq, wi.grid_time, wi.work_to_do_type,
         min(event_time) as earliest_event_time, count(*) as tile_count,
         (SELECT count(member_id)
          FROM team_member tm
          WHERE wi.team_id = tm.team_id) as team_size
  from work_item_to_aggregate wi
  where wi.processing_state = 'Ready'
  group by wi.team_id, wi.zoom_level, wi.tile_seq, wi.grid_time, wi.work_to_do_type
  order by earliest_event_time;
;


