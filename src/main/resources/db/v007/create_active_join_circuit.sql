--liquibase formatted sql

--changeset dreamscale:5

create table active_join_circuit (
    id uuid primary key not null,
    organization_id uuid not null,
    member_id uuid not null,
    joined_circuit_id uuid not null,
    joined_circuit_owner_id uuid not null,
    joined_circuit_type text,
    join_type text,
    join_date timestamp
 );
