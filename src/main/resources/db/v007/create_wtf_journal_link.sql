--liquibase formatted sql

--changeset dreamscale:5

create table wtf_journal_link (
    id uuid primary key not null,
    organization_id uuid not null,
    member_id uuid not null,
    project_id uuid ,
    task_id uuid ,
    intention_id uuid not null,
    wtf_circuit_id uuid not null
);

