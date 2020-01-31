--liquibase formatted sql

--changeset dreamscale:4

create table team_circuit_room (
 id uuid primary key not null,
 organization_id uuid,
 team_id uuid unique,
 talk_room_id uuid,
 local_name text,
 description text,
 jsonTags text
);

