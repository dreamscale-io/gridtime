--liquibase formatted sql

--changeset dreamscale:4

alter table talk_room
    add column moderator_id uuid;
