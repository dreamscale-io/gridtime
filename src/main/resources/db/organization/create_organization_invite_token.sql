--liquibase formatted sql

--changeset dreamscale:0
create table organization_invite_token (
  id uuid primary key not null,
  token text,
  organization_id uuid,
  expiration_date timestamp
);

