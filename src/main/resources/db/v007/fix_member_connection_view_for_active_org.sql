--liquibase formatted sql

--changeset dreamscale:5

drop view member_connection_view;

create view member_connection_view as
  select om.organization_id, om.id member_id, r.id root_account_id, aas.connection_id,
  aas.last_heartbeat, aas.last_activity, om.username, r.display_name, r.full_name
  from organization_member om, root_account r, active_account_status aas
  where om.root_account_id = r.id and om.root_account_id = aas.root_account_id
    and aas.logged_in_organization_id = om.organization_id and aas.connection_id is not null;