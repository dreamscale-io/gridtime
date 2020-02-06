--liquibase formatted sql

--changeset dreamscale:4

alter table talk_room
drop column owner_id;

alter table talk_room
drop column moderator_id;

alter table team_circuit_room
add column open_time timestamp;

alter table team_circuit_room
add column close_time timestamp;

alter table team_circuit_room
add column circuit_status text;

alter table team_circuit_room
add column json_tags text;

alter table team_circuit_room
add column owner_id uuid;

alter table team_circuit_room
add column moderator_id uuid;

