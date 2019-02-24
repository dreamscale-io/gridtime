
--liquibase formatted sql

--changeset dreamscale:2
alter table circle
    add column  owner_member_id  uuid;
