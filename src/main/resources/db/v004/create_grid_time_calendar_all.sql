--liquibase formatted sql

--changeset dreamscale:4
create table grid_time_calendar (
  id uuid,
  zoom_level text,
  tile_seq int not null,
  clock_time timestamp,
  grid_time text,
  year int,
  block int,
  block_week int,
  day int,
  day_part int,
  twenty_of_twelve int
);
