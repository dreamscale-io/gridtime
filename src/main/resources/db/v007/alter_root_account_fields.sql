--liquibase formatted sql

--changeset dreamscale:5

alter table root_account
    add column registration_date timestamp ;

alter table root_account
    drop column activation_code ;

