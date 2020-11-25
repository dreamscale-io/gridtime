--liquibase formatted sql

--changeset dreamscale:5

create table terminal_circuit_command_history (
    id uuid primary key not null,
    circuit_id uuid not null,
    command text,
    args text,
    command_date timestamp
 );

create table terminal_circuit_location_history (
    id uuid primary key not null,
    circuit_id uuid not null,
    zoom_level text,
    tile_seq integer,
    movement_date timestamp
 );
