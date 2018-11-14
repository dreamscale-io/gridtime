--liquibase formatted sql

--changeset dreamscale:1
drop view team_member_work_status_view;

create view team_member_work_status_view as
select tm.member_id id, tm.team_id, om.organization_id, om.email, m.full_name, m2.mood_rating, xp.total_xp, a.last_activity, a.active_status,
  w.active_task_id, t.name as active_task_name, t.summary as active_task_summary, w.working_on, w.spirit_status, w.spirit_message, w.active_session_id, w.active_session_start
from team_member tm
join organization_member om on tm.member_id = om.id
join master_account m on om.master_account_id = m.id
join active_account_status a on om.master_account_id = a.master_account_id
left outer join mood m2 on tm.member_id = m2.member_id
left outer join team_member_xp xp on tm.member_id = xp.member_id
left outer join active_work_status w on om.id = w.member_id
left outer join task t on w.active_task_id = t.id
order by xp.total_xp desc;

