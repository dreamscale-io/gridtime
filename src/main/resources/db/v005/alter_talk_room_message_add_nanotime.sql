--liquibase formatted sql

--changeset dreamscale:4
alter table talk_room_message
    add column nano_time bigint;