--liquibase formatted sql

--changeset dreamscale:3
create table torchie_bookmark (
  torchie_id uuid primary key not null,
  bookmark_source_time timestamp,
  bookmark_source_sequence integer,
  last_tile_completed timestamp
);
