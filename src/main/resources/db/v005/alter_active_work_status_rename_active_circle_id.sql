--liquibase formatted sql

--changeset dreamscale:4

alter table active_work_status
    rename column active_circle_id to active_circuit_id;
