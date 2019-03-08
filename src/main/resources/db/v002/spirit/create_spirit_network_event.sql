--liquibase formatted sql

--changeset dreamscale:2
create table spirit_network_event (
  id uuid primary key not null,
  position timestamp,
  event_type text,
  description text,
  network_id uuid,
  source_spirit uuid,
  connected_spirits text,
  metadata text
);