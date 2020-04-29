--liquibase formatted sql

--changeset dreamscale:5

  create table organization_subscription_seat (
    id uuid primary key not null,
    subscription_id uuid not null,
    organization_id uuid not null,
    root_account_id uuid not null,
    org_email text,
    activation_date timestamp,
    cancel_date timestamp,
    subscription_status text not null
 );
