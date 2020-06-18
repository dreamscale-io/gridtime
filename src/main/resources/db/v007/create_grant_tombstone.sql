--liquibase formatted sql

--changeset dreamscale:5

create table project_grant_tombstone (
    id uuid primary key not null,
    grant_id uuid not null,
    organization_id uuid not null,
    project_id uuid not null,
    project_name text,
    grant_type text not null,
    granted_to_id uuid not null,
    granted_by_id uuid not null,
    granted_date timestamp,
    rip_date timestamp
);


