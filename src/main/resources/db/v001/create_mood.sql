--liquibase formatted sql

--changeset dreamscale:1
create table mood (
  member_id uuid primary key not null,
  mood_rating integer,
  organization_id uuid
);

