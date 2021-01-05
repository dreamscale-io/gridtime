--liquibase formatted sql

--changeset dreamscale:5

create table grid_tile_carry_over_context (
    id uuid primary key not null,
    torchie_id uuid not null,
    calendar_id uuid not null,
    json text,
    constraint carry_over_unique_key unique (torchie_id, calendar_id)
 );
