--liquibase formatted sql

--changeset dreamscale:2

drop view member_status_view;

create view member_status_view as
select om.id, om.organization_id, om.email, m.full_name, xp.total_xp, a.last_activity, a.online_status,
  w.active_task_id, t.name as active_task_name, t.summary as active_task_summary, w.working_on, w.active_circuit_id, om.username
from organization_member om
join root_account m on om.root_account_id = m.id
left outer join active_account_status a on om.root_account_id = a.root_account_id
left outer join spirit_xp xp on om.id = xp.member_id
left outer join active_work_status w on om.id = w.member_id
left outer join task t on w.active_task_id = t.id;

