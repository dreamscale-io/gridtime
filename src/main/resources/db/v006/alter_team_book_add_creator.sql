--liquibase formatted sql

--changeset dreamscale:4

alter table team_book
    add column created_by_member_id uuid;

alter table team_book_word
    add column pulled_by_member_id uuid;

alter table team_dictionary_word
    add column created_by_member_id uuid;

alter table team_dictionary_word
    add column last_modified_by_member_id uuid;

alter table team_dictionary_word_tombstone
    add column rip_by_member_id uuid;

alter table team_book_word_tombstone
    add column rip_by_member_id uuid;

alter table team_book_word_override
    add column created_by_member_id uuid;

alter table team_book_word_override
    add column last_modified_by_member_id uuid;

