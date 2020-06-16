--liquibase formatted sql

--changeset dreamscale:5

 create table project_grant_access (
    id uuid primary key not null,
    project_id uuid not null,
    access_type text not null,
    access_id uuid not null,
    granted_by_id uuid not null,
    granted_date timestamp
 );

 alter table project add created_by uuid;

 alter table project add created_date timestamp;