--liquibase formatted sql

--changeset dreamscale:4

create table team_member_home (
   id uuid primary key not null,
   organization_id uuid,
   member_id uuid,
   home_team_id uuid,
   last_modified_date timestamp,
   constraint team_member_unique_home_key unique (member_id)
 );

