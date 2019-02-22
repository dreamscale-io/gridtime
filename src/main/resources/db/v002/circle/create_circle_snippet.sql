--liquibase formatted sql

--changeset dreamscale:2
create table circle_snippet (
  id uuid primary key not null,
  circle_id uuid,
  time_position    timestamp without time zone,
  snippet_type text,
  metadata      text
);

