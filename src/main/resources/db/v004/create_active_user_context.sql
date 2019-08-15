--liquibase formatted sql

--changeset dreamscale:4
create table active_user_context (
  master_account_id uuid primary key not null,
  organization_id uuid not null,
  member_id uuid not null,
  team_id uuid
);