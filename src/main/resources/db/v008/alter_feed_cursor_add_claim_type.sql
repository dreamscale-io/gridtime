--liquibase formatted sql

--changeset dreamscale:5

alter table torchie_feed_cursor add column claim_type text;

