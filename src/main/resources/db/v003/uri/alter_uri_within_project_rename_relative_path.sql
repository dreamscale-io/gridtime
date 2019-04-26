--liquibase formatted sql

--changeset dreamscale:3
alter table uri_within_project
  rename column relativePath to relative_path;
