--liquibase formatted sql

--changeset dreamscale:2
alter table circle_context
    rename column member_id to spirit_id;