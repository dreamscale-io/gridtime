--liquibase formatted sql

--changeset dreamscale:2
alter table circle_feed
    rename column member_id to spirit_id;