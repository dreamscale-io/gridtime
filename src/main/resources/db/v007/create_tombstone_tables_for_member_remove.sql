--liquibase formatted sql

--changeset dreamscale:5

alter table team_member
add column join_date timestamp;

 create table organization_member_tombstone (
  id uuid primary key not null,
  member_id uuid not null,
  organization_id uuid not null,
  email text,
  username text,
  display_name text,
  full_name text,
  join_date timestamp,
  rip_date timestamp
 );

 create table team_member_tombstone (
    id uuid primary key not null,
    organization_id uuid not null,
    member_id uuid not null,
    team_id uuid not null,
    email text,
    username text,
    display_name text,
    full_name text,
    join_date timestamp,
    rip_date timestamp
 );

