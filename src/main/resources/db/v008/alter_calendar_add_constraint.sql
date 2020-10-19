--liquibase formatted sql

--changeset dreamscale:5

alter table grid_time_calendar add constraint grid_time_ux unique (zoom_level, clock_time);