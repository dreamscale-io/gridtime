--liquibase formatted sql

--changeset dreamscale:2
create table torchie_tombstone (
  id uuid primary key not null,
  spirit_id uuid,
  epitaph text,
  date_of_birth timestamp,
  date_of_death timestamp,

  level integer,
  total_xp integer,
  title text
);
