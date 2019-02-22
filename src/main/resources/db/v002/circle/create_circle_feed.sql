--liquibase formatted sql

--changeset dreamscale:2
create table circle_feed (
  id uuid primary key not null,
  circle_id uuid,
  member_id uuid,
  time_position    timestamp without time zone,
  message_type text,
  metadata      text
);

