--liquibase formatted sql

--changeset dreamscale:1
create table wtf_session (
  id  uuid primary key not null,
  member_id uuid not null,
  organization_id uuid,
  start_time    timestamp without time zone,
  end_time      timestamp without time zone,
  problem_statement text,
  resolution text
);

