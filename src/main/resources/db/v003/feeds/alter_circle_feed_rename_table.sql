--liquibase formatted sql

--changeset dreamscale:3
alter table circle_feed
    rename to circle_message;