--liquibase formatted sql

--changeset dreamscale:5

create table team_project (
    id uuid primary key not null,
    organization_id uuid not null,
    team_id uuid not null,
    creator_id uuid ,
    name text not null,
    lowercase_name text not null,
    description text,
    creation_date timestamp
 );

create table team_task (
    id uuid primary key not null,
    organization_id uuid not null,
    team_id uuid not null,
    team_project_id uuid not null,
    creator_id uuid ,
    name text not null,
    lowercase_name text not null,
    description text,
    creation_date timestamp
 );

-- insert into team_project (id, organization_id, team_id, creator_id, name, lowercase_name, description, creation_date)
-- select p.id, p.organization_id, tm.team_id, null creator_id,
-- p.name, lower(p.name) lowercase_name, null description, now() creation_date from project p,
-- (select organization_id, cast(max(cast(id as text)) as uuid) team_id from team group by organization_id) tm
-- where p.organization_id = tm.organization_id ;
--
--
-- insert into team_task (id, organization_id, team_id, team_project_id, creator_id, name, lowercase_name, description, creation_date)
-- select t.id, t.organization_id, tm.team_id, null creator_id,
-- t.name, lower(t.name) lowercase_name, null description, now() creation_date from task t,
-- (select organization_id, cast(max(cast(id as text)) as uuid) team_id from team group by organization_id) tm
-- where p.organization_id = tm.organization_id ;
