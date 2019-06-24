--liquibase formatted sql

--changeset dreamscale:4
create table grid_time_twenties (
  tile_seq bigint primary key not null,
  clock_time timestamp,
  grid_time text,
  year int,
  block int,
  block_week int,
  day int,
  day_part int,
  twenty_of_twelve int
);

create table grid_time_dayparts (
  tile_seq bigint primary key not null,
  clock_time timestamp,
  grid_time text,
  year int,
  block int,
  block_week int,
  day int,
  day_part int
);

create table grid_time_days (
  tile_seq bigint primary key not null,
  clock_time timestamp,
  grid_time text,
  year int,
  block int,
  block_week int,
  day int
);

create table grid_time_weeks (
  tile_seq bigint primary key not null,
  clock_time timestamp,
  grid_time text,
  year int,
  block int,
  block_week int
);
