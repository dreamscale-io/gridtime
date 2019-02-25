--liquibase formatted sql

--changeset dreamscale:2
alter table active_account_status
    rename column active_status to online_status;