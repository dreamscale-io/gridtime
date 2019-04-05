--liquibase formatted sql

--changeset dreamscale:3
alter table circle_member
    rename column spirit_id to torchie_id;