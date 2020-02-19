--liquibase formatted sql

--changeset dreamscale:4

drop table team_dictionary_tag;

create table team_dictionary_word (
   id uuid primary key not null,
   organization_id uuid,
   team_id uuid,
   word_name text,
   lower_case_word_name text,
   definition text,
   creation_date timestamp,
   last_modified_date timestamp,
   constraint team_word_unique_key unique (team_id, lower_case_word_name)
 );


drop table community_dictionary_tag;

 create table community_dictionary_word (
   id uuid primary key not null,
   word_name text,
   lower_case_word_name text,
   definition text,
   creation_date timestamp,
   last_modified_date timestamp,
   constraint community_word_unique_key unique (lower_case_word_name)
 );

 drop table team_book_tag;

 create table team_book_word (
  id uuid primary key not null,
  team_book_id uuid,
  team_word_id uuid,
  pull_date timestamp,
  modified_status text,
  constraint team_book_word_unique_key unique (team_book_id, team_word_id)
);

 drop table community_book_tag;

create table community_book_word (
  id uuid primary key not null,
  community_book_id uuid,
  community_word_id uuid,
  pull_date timestamp,
  constraint community_book_word_unique_key unique (community_book_id, community_word_id)
);

 drop table team_dictionary_tombstone;

 create table team_dictionary_word_tombstone (
    id uuid primary key not null,
    organization_id uuid,
    team_id uuid,
    dead_word_name text,
    dead_definition text,
    lower_case_word_name text,
    rip_date timestamp,
    revive_date timestamp,
    forward_to uuid
 );

 drop table team_book_override;

create table team_book_word_override (
  team_book_word_id uuid primary key not null,
  word_name text,
  definition text,
  override_date timestamp,
  last_modified_date timestamp
);

 create table team_book_word_tombstone (
    id uuid primary key not null,
    dead_word_name text,
    dead_definition text,
    lower_case_word_name text,
    rip_date timestamp
 );

 drop table community_dictionary_tombstone;

 create table community_dictionary_tombstone (
    id uuid primary key not null,
    dead_word_name text,
    dead_definition text,
    lower_case_word_name text,
    rip_date timestamp,
    revive_date timestamp,
    forward_to uuid
 );

 drop table team_dictionary_promotion;

 create table team_dictionary_word_promotion (
   id uuid primary key not null,
   organization_id uuid,
   team_id uuid,
   word_name text,
   definition text,
   promotion_status text,
   promotion_date timestamp,
   response_date timestamp
 );
