--liquibase formatted sql

--changeset dreamscale:5

 create table organization_member_tombstone (
  member_id uuid primary key not null,
  root_account_id uuid,
  email text,
  username text,
  external_id text,
  organization_id uuid,
  rip_date timestamp
 );

 create table team_member_tombstone (
    id uuid primary key not null,
    organization_id uuid not null,
    member_id uuid not null,
    team_id uuid not null,
    rip_date timestamp
 );

}