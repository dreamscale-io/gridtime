--liquibase formatted sql

--changeset dreamscale:5

create table ticket_tombstone (
    id uuid primary key not null,
    ticket_id uuid not null,
    owner_id uuid not null,
    ticket_type text not null,
    ticket_code text not null,
    json_props text,
    issue_date timestamp,
    expiration_date timestamp,
    used_by uuid,
    rip_date timestamp,
    rip_type text
);

create table root_account_tombstone (
    id uuid primary key not null,
    root_account_id uuid not null,
    root_email text not null,
    root_username text not null,
    lowercase_root_username text not null,
    full_name text,
    display_name text,
    registration_date timestamp,
    activation_date timestamp,
    last_updated timestamp,
    is_email_validated boolean,
    api_key text,
    rip_date timestamp
);

