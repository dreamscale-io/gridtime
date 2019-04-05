--liquibase formatted sql

--changeset dreamscale:3
alter table member_name_view
    rename column spirit_id to torchie_id;