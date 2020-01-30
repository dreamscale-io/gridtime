--liquibase formatted sql

--changeset dreamscale:4
alter table learning_circuit
    add column description text;

alter table learning_circuit
    add column json_tags text;
