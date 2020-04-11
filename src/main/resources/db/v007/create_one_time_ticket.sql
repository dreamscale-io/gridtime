--liquibase formatted sql

--changeset dreamscale:5

 create table one_time_ticket (
    id uuid primary key not null,
    owner_id uuid not null,
    ticket_type text not null,
    ticket_code text not null,
    json_props text,
    issue_date timestamp,
    expiration_date timestamp
 );
