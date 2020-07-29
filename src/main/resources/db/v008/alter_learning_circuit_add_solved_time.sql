--liquibase formatted sql

--changeset dreamscale:5

alter table learning_circuit add solved_time timestamp;