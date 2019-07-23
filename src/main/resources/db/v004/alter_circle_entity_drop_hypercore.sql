--liquibase formatted sql

--changeset dreamscale:4
alter table circle
    drop column hypercore_feed_id;

alter table circle
    drop column hypercore_public_key;

alter table circle
    drop column hypercore_secret_key;