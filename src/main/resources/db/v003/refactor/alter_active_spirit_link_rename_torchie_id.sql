--liquibase formatted sql

--changeset dreamscale:3
alter table active_spirit_link
    rename column spirit_id to torchie_id;