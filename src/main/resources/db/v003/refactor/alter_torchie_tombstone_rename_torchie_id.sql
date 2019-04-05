--liquibase formatted sql

--changeset dreamscale:3
alter table torchie_tombstone
    rename column spirit_id to torchie_id;