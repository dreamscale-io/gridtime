--liquibase formatted sql

--changeset dreamscale:1
alter table intention
    add column finish_status text;

alter table intention
    add column finish_time timestamp;

update intention
    set finish_status = 'done';