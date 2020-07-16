--liquibase formatted sql

--changeset dreamscale:5

create table circuit_mark (
    id uuid primary key not null,
    organization_id uuid not null,
    member_id uuid not null,
    circuit_id uuid not null,
    mark_type text,
    created_date timestamp,
    constraint circuit_mark_unique_key unique (organization_id, member_id, circuit_id)
 );
