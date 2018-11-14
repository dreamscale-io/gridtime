--liquibase formatted sql

--changeset dreamscale:1
alter table active_work_status
    add column spirit_status text;

alter table active_work_status
    add column spirit_message text;

alter table active_work_status
    add column active_session_id uuid;

alter table active_work_status
    add column active_session_start timestamp without time zone;