--liquibase formatted sql

--changeset dreamscale:4

drop table team_learning_circuit;
drop table team_circuit_input_circuit_event;

create table team_circuit (
 id uuid primary key not null,
 organization_id uuid,
 team_id uuid unique,
 team_room_id uuid,
 owner_id uuid,
 moderator_id uuid
);

