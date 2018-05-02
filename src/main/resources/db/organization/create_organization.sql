--liquibase formatted sql

--changeset dreamscale:0
create table organization (
  id uuid primary key not null,
  org_name text,
  domain_name text,
  jira_site_url text,
  jira_user text,
  jira_api_key text
);
