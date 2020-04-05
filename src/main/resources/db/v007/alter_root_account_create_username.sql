--liquibase formatted sql

--changeset dreamscale:5

alter table root_account
    add column root_user_name text;

alter table root_account
    add column lower_case_root_user_name text;

alter table root_account
    add column last_updated timestamp;

alter table root_account
    add column is_email_validated boolean not null default 'false';

alter table root_account
  add constraint root_user_name_unique_key unique (lower_case_root_user_name);