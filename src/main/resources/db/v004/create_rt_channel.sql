--liquibase formatted sql

--changeset dreamscale:4
create table realtime_channel (
  id uuid primary key not null,
  organization_id uuid,
  owner_id uuid,
  channel_type text
);