--liquibase formatted sql

--changeset dreamscale:5

truncate table terminal_circuit_command_history;

drop table terminal_circuit;

create table terminal_circuit (
 id uuid primary key not null,
 organization_id uuid,
 creator_id uuid,
 talk_room_id uuid,
 circuit_name text,
 created_date timestamp,
 constraint terminal_name_unique_key unique(organization_id, circuit_name)
);
