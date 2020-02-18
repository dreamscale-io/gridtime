--liquibase formatted sql

--changeset dreamscale:4

alter table team_dictionary_tag
    add column lower_case_tag_name text;

alter table team_dictionary_tag
  drop constraint team_tag_unique_key;

  alter table team_dictionary_tag
  add constraint team_tag_unique_key unique (team_id, lower_case_tag_name);