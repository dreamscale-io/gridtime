--liquibase formatted sql

--changeset dreamscale:4

alter table talk_room
    add column room_name text;

UPDATE talk_room
SET room_name = talk_room_id
    WHERE
    room_name is null;

alter table talk_room
    drop column talk_room_id;

alter table talk_room
    add constraint tr_unique_room unique (organization_id, room_name);

