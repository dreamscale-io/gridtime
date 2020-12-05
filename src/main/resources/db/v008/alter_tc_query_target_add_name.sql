--liquibase formatted sql

--changeset dreamscale:5

alter table terminal_circuit_query_target
add column target_name text;