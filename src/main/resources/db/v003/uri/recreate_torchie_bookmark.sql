--liquibase formatted sql

--changeset dreamscale:3
drop table torchie_bookmark;

create table torchie_bookmark (
  torchie_id uuid not null,
  metronome_cursor timestamp
);
