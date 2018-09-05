--liquibase formatted sql

--changeset dreamscale:0
create table team_member (
  id uuid primary key not null,
  organization_id uuid,
  team_id uuid,
  member_id uuid
);

