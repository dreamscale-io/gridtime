--liquibase formatted sql

--changeset dreamscale:5

truncate table terminal_circuit_location_history CASCADE ;

alter table terminal_circuit_location_history add column organization_id uuid not null;