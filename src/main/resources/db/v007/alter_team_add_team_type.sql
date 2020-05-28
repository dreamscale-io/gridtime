--liquibase formatted sql

--changeset dreamscale:5

alter table team add team_type text default 'OPEN';

update team set team_type = 'OPEN';
