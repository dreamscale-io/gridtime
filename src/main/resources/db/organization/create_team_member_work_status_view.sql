--liquibase formatted sql

--changeset dreamscale:0
create view team_member_work_status_view as
select tm.member_id id, tm.team_id, om.organization_id, om.email, m.full_name, a.last_activity, a.active_status,
  w.active_task_id, t.name as active_task_name, t.summary as active_task_summary, w.working_on
from team_member tm
join organization_member om on tm.member_id = om.id
join master_account m on om.master_account_id = m.id
join active_account_status a on om.master_account_id = a.master_account_id
left outer join active_work_status w on om.id = w.member_id
left outer join task t on w.active_task_id = t.id;

