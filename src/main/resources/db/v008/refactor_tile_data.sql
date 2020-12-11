--liquibase formatted sql

--changeset dreamscale:5

truncate table grid_row;

alter table grid_row add column row_index integer not null;