--liquibase formatted sql

--changeset dreamscale:0
create table flow_activity (
  id bigint constraint activity_pk primary key,
  member_id     uuid             not null,
  start_time    timestamp without time zone,
  end_time      timestamp without time zone,
  duration      integer,
  activity_type text,
  metadata      text
);

