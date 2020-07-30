--liquibase formatted sql

--changeset dreamscale:5

delete from active_account_status;
