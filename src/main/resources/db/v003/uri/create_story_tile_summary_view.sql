--liquibase formatted sql

--changeset dreamscale:3
create view story_tile_summary_view as
  select id, torchie_id, uri, zoom_level, clock_position, dream_time,
         year, block, weeks_into_block, weeks_into_year, days_into_week, four_hour_steps, twenty_minute_steps,
  json_tile_summary
      from story_tile;