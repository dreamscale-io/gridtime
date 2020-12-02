--liquibase formatted sql

--changeset dreamscale:5


create table terminal_circuit_query_target (
    id uuid primary key not null,
    circuit_id uuid not null,
    organization_id uuid not null,
    target_type text,
    target_id uuid,
    target_date timestamp
 );


