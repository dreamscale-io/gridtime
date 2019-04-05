--liquibase formatted sql

--changeset dreamscale:3
alter table circle_feed
    rename column time_position to position;