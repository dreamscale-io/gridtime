--liquibase formatted sql

--changeset dreamscale:4
create table work_item_to_aggregate (
  id uuid primary key not null,
  source_torchie_id uuid,
  team_id uuid,
  zoom_level text,
  tile_seq bigint,
  grid_time text,
  event_time timestamp,
  processing_state text,
  work_to_do_type text,
  claiming_worker_id uuid
);

--exlock, to grab a claim, select exlock, select * where time = min(event_time) and not in progress, update all in progress matching zoom & tileseq
--release exlock

-- in general blocking until lock can be aquired is okay, events can still be added to the table concurrently,
-- only work claiming will block on consuming work to do.

-- then all the stuff to generate, can be processed in parallel... can it?

-- what happens if I get two claims for the same tile time?



-- individual feeds can put tiles in here, at any aggregate level, that indicate a rollup at the team level
-- should be made for the same tile

-- public class TileStreamEvent {
--
-- UUID torchieId;
-- GeometryClock.GridTime gridTime;
-- EventType eventType;
-- }
