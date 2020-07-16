--liquibase formatted sql

--changeset dreamscale:5

alter table learning_circuit
    add column marks_for_review integer ;
