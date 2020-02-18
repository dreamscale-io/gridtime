--liquibase formatted sql

--changeset dreamscale:4

alter table team_dictionary_tombstone
    add column revive_date timestamp ;

alter table team_dictionary_tombstone
    add column organization_id uuid ;

alter table team_dictionary_tombstone
    add column team_id uuid ;

alter table team_dictionary_tombstone
    add column lower_case_tag_name text ;