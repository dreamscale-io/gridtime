--liquibase formatted sql

--changeset dreamscale:5

alter table grid_calendar add column grid_coords text;