--liquibase formatted sql

--changeset dreamscale:2
create table active_spirit_link (
  id uuid primary key not null,
  network_id uuid,
  spirit_id uuid
);