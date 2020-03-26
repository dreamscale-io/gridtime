--liquibase formatted sql

--changeset dreamscale:4

alter table learning_circuit
    add column status_room_id uuid;