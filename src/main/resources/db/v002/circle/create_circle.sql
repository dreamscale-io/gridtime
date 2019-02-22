--liquibase formatted sql

--changeset dreamscale:2
create table circle (
  id uuid primary key not null,
  circle_name text,
  public_key text,
  private_key text
);

