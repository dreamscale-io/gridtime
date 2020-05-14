--liquibase formatted sql

--changeset dreamscale:5

drop view organization_subscription_details_view;

create view organization_subscription_details_view as (
  select os.id, os.root_account_owner_id, os.organization_id, os.total_seats, os.seats_remaining, os.require_member_email_in_domain, os.creation_date,
  os.subscription_status, o.org_name organization_name, o.domain_name
  from organization_subscription os, organization o
  where os.organization_id = o.id
);

drop table organization_invite_token;