--liquibase formatted sql

--changeset dreamscale:2
alter table flow_activity
    add column component text;