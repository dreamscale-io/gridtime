--liquibase formatted sql

--changeset dreamscale:5

 create table organization_subscription (
    id uuid primary key not null,
    root_account_owner_id uuid not null,
    organization_id uuid not null,
    total_seats integer not null,
    seats_remaining integer not null,
    require_member_email_in_domain boolean not null,
    stripe_payment_id text,
    stripe_customer_id text,
    creation_date timestamp not null,
    last_modified_date timestamp ,
    last_status_check timestamp ,
    subscription_status text not null
 );


create view organization_subscription_details_view as (
  select os.id, os.root_account_owner_id, os.organization_id, os.total_seats, os.seats_remaining, os.require_member_email_in_domain, os.creation_date,
  os.subscription_status, o.org_name organization_name, o.domain_name, oit.token invite_token
  from organization_subscription os, organization o, organization_invite_token oit
  where os.organization_id = o.id and os.organization_id = oit.organization_id
);
