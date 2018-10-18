--liquibase formatted sql

--changeset dreamscale:1
alter table intention
    add column flame_rating int;