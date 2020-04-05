--liquibase formatted sql

--changeset dreamscale:5

alter table team
drop constraint team_name_unique_key;

alter table team
add constraint team_name_unique_key unique (organization_id, lower_case_name);
