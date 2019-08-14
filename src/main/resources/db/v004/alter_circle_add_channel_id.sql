--liquibase formatted sql

--changeset dreamscale:4
alter table circle
    add column channel_id uuid;