--liquibase formatted sql

--changeset dreamscale:4

alter table master_account
    add column short_name text;

alter table active_account_status
  add column last_heartbeat timestamp;


create view member_connection_view as
  select om.organization_id, om.id member_id, aas.connection_id, aas.last_heartbeat, aas.last_activity, m.short_name, m.full_name
  from organization_member om, master_account m, active_account_status aas
  where om.master_account_id = m.id and om.master_account_id = aas.master_account_id
    and aas.connection_id is not null;

