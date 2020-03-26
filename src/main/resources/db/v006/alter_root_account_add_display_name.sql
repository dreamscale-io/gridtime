--liquibase formatted sql

--changeset dreamscale:4

alter table root_account
    add column display_name text;