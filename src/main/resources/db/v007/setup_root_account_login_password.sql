--liquibase formatted sql

--changeset dreamscale:5

CREATE EXTENSION pgcrypto;

alter table root_account
    add column crypt_password text;