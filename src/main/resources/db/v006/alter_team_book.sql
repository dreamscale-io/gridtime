--liquibase formatted sql

--changeset dreamscale:4

alter table team_book
    add column lower_case_book_name text;
