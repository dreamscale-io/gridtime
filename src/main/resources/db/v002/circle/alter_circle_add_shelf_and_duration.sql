
--liquibase formatted sql

--changeset dreamscale:2
alter table circle
add column last_shelf_time    timestamp without time zone;

alter table circle
    add column last_resume_time    timestamp without time zone;

alter table circle
    add column  on_shelf  boolean;

alter table circle
    add column  problem_description  text;

alter table circle
add column  duration_in_seconds  integer;
