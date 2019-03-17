--liquibase formatted sql

--changeset dreamscale:2
alter table intention
    add column linked boolean;