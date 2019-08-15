--liquibase formatted sql

--changeset dreamscale:4
create table realtime_channel_member (
  id uuid primary key not null,
  channel_id uuid,
  join_time timestamp,
  organization_id uuid,
  member_id uuid,
  team_id uuid
);

--next is making entity objects for this, then implementing join and leave in the code
--write integration test for this
--publishing the join/leave APIs to heroku