--liquibase formatted sql

--changeset dreamscale:3
alter table journal_link_event
  drop column linked_members;

alter table journal_link_event
  add column linked_members text;