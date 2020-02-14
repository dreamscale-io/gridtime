--liquibase formatted sql

--changeset dreamscale:4

create table team_dictionary_tag (
   id uuid primary key not null,
   organization_id uuid,
   team_id uuid,
   tag_name text,
   description text,
   creation_date timestamp,
   last_modified_date timestamp,
   constraint team_tag_unique_key unique (team_id, tag_name)
 );

 create table team_dictionary_tombstone (
    id uuid primary key not null,
    dead_tag_name text,
    dead_description text,
    rip_date timestamp,
    forward_to uuid
 );

create table team_book (
  id uuid primary key not null,
  organization_id uuid,
  team_id uuid,
  book_name text,
  creation_date timestamp ,
  last_modified_date timestamp ,
  constraint team_book_unique_key unique (team_id, book_name)
);

create table team_book_tag (
  id uuid primary key not null,
  team_book_id uuid,
  team_tag_id uuid,
  pull_date timestamp,
  modified_status text,
  constraint team_book_tag_unique_key unique (team_book_id, team_tag_id)
);

create table team_book_override (
  team_book_tag_id uuid primary key not null,
  tag_name text,
  description text,
  override_date timestamp,
  last_modified_date timestamp
);

create table community_book (
  id uuid primary key not null,
  book_name text,
  creation_date timestamp ,
  last_modified_date timestamp ,
  constraint community_book_unique_key unique (book_name)
);

create table community_book_tag (
  id uuid primary key not null,
  community_book_id uuid,
  community_tag_id uuid,
  pull_date timestamp,
  constraint community_book_tag_unique_key unique (community_book_id, community_tag_id)
);

 create table team_dictionary_promotion (
   id uuid primary key not null,
   organization_id uuid,
   team_id uuid,
   tag_name text,
   description text,
   promotion_status text,
   promotion_date timestamp,
   response_date timestamp
 );

 create table community_dictionary_tag (
   id uuid primary key not null,
   tag_name text,
   description text,
   creation_date timestamp,
   last_modified_date timestamp,
   constraint community_tag_unique_key unique (tag_name)
 );

 create table community_dictionary_tombstone (
    id uuid primary key not null,
    dead_tag_name text,
    dead_description text,
    rip_date timestamp,
    forward_to uuid
 );
