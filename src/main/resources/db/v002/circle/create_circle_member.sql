--liquibase formatted sql

--changeset dreamscale:2
create table circle_member (
  id uuid primary key not null,
  circle_id uuid,
  member_id uuid
);

