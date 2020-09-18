--liquibase formatted sql

--changeset dreamscale:5

create table terminal_circuit (
 id uuid primary key not null,
 organization_id uuid,
 creator_id uuid unique,
 talk_room_id uuid,
 circuit_name text,
 created_date timestamp,
 constraint terminal_name_unique_key unique(organization_id, circuit_name)
);
