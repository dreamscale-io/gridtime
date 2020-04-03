--liquibase formatted sql

--changeset dreamscale:4

alter table team
    add column lower_case_name text;


update team set lower_case_name = lower(name);