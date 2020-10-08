--liquibase formatted sql

--changeset dreamscale:5

drop table torchie_feed_cursor;

create table torchie_feed_cursor (
  id uuid primary key not null,
  torchie_id uuid not null,
  team_id uuid,
  organization_id uuid,
  last_published_data_cursor timestamp, -- ending data available
  last_tile_processed_cursor timestamp, -- if this is a beginning of a tile
  next_wait_until_cursor timestamp,
  first_tile_position timestamp,
  claiming_server_id uuid,
  last_claim_update timestamp,
  failure_count integer
);

--|-------|-------|
--^
-----^ (wait until I get data beyond next date)
------------^ (now ready)

-- when published data exists after next wait until

--when I have data after the next end cap.

--when published data

--select the jobs not in the table, and add them into the table

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
