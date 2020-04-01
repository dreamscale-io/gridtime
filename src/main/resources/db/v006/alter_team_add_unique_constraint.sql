--liquibase formatted sql

--changeset dreamscale:4

alter table team
add constraint team_name_unique_key unique (organization_id, name);

alter table team
add column creator_id uuid;

