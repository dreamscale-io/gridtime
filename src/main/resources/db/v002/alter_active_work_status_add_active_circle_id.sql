--liquibase formatted sql

--changeset dreamscale:2

alter table active_work_status
  add column active_circle_id uuid;

alter table active_work_status
  add column active_circle_start timestamp without time zone;
