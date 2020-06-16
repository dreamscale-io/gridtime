--liquibase formatted sql

--changeset dreamscale:5

alter table project add lowercase_name text;

alter table project add description text;

alter table project add is_private boolean;

alter table task add lowercase_name text;

alter table task rename summary to description;


drop table team_project;

drop table team_task;




