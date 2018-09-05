--liquibase formatted sql

--changeset dreamscale:0
create table team (
  id uuid primary key not null,
  name text,
  organization_id uuid
);

