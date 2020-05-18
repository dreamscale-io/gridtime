--liquibase formatted sql

--changeset dreamscale:5

  create table active_join_circuit (
    id uuid primary key not null,
    organization_id uuid not null,
    member_id uuid not null,
    team_id_of_invocation uuid not null,
    join_date timestamp,
    owner_type text,
    circuit_type text
 );
