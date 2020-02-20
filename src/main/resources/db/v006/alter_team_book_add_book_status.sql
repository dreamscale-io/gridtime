--liquibase formatted sql

--changeset dreamscale:4

alter table team_book
    add column book_status text;