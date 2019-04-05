--liquibase formatted sql

--changeset dreamscale:3
alter table circle_message
    rename column spirit_id to torchie_id;