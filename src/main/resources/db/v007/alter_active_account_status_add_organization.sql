--liquibase formatted sql

--changeset dreamscale:5

alter table active_account_status
    add column logged_in_organization_id uuid;