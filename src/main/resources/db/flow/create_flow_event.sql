--liquibase formatted sql

--changeset dreamscale:0
create table flow_event (
  id bigint constraint event_pk primary key,
  member_id     uuid             not null,
  time_position    timestamp without time zone,
  event_type text,
  metadata      text
);
