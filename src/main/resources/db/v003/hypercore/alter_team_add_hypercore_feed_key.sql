--liquibase formatted sql

--changeset dreamscale:3
alter table team
    add column hypercore_feed_id text;

alter table team
    add column hypercore_public_key text;

alter table team
    add column hypercore_secret_key text;