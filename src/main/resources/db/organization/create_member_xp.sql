--liquibase formatted sql

--changeset dreamscale:0
create table team_member_xp (
  id uuid primary key not null,
  organization_id uuid,
  member_id uuid,
  total_xp int
);

