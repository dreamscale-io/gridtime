--liquibase formatted sql

--changeset dreamscale:3
alter table circle
    drop column public_key;

alter table circle
    drop column private_key;

alter table circle
    add column hypercore_feed_id text;

alter table circle
    add column hypercore_public_key text;

alter table circle
    add column hypercore_secret_key text;