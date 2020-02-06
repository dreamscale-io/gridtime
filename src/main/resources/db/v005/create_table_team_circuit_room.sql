--liquibase formatted sql

--changeset dreamscale:4

create table team_circuit_room (
 id uuid primary key not null,
 organization_id uuid,
 team_id uuid,
 talk_room_id uuid,
 local_name text,
 description text,
 jsonTags text,
 constraint team_room_unique_key unique (team_id, local_name)
);

