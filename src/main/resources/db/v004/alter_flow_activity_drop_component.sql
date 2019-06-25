--liquibase formatted sql

--changeset dreamscale:4
alter table flow_activity
    drop column component;