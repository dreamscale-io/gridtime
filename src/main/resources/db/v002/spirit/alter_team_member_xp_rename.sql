--liquibase formatted sql

--changeset dreamscale:2
alter table team_member_xp rename to spirit_xp;

alter table spirit_xp
  rename column member_id to spirit_id;

