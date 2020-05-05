--liquibase formatted sql

--changeset dreamscale:5

alter table organization_member
    add column lower_case_user_name text ;

alter table organization_member
  add column last_updated timestamp;
