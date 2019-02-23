
--liquibase formatted sql

--changeset dreamscale:2
alter table circle
add column start_time    timestamp without time zone;

alter table circle
add column  end_time      timestamp without time zone;

alter table circle
    add column   organization_id uuid;