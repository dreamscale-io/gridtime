--liquibase formatted sql

--changeset dreamscale:3
create table journal_link_event (
  id uuid primary key not null,
  position timestamp,
  member_id uuid not null,
  intention_id uuid not null,
  linked_members json
);
