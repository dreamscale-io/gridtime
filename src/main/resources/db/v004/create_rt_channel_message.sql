--liquibase formatted sql

--changeset dreamscale:4
create table realtime_channel_message (
  id uuid primary key not null,
  channel_id uuid,
  from_member_id uuid,
  message_time timestamp,
  message_type text,
  json text
);