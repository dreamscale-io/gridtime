--liquibase formatted sql

--changeset dreamscale:0
create view member_status_view as
select o.id, o.email, m.full_name, a.last_activity, a.active_status, o.organization_id
from organization_member o, master_account m, active_account_status a
where o.master_account_id = a.master_account_id
and o.master_account_id = m.id;

