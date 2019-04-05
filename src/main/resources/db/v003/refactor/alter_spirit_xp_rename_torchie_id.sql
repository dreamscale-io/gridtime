--liquibase formatted sql

--changeset dreamscale:3
alter table spirit_xp
    rename column spirit_id to torchie_id;