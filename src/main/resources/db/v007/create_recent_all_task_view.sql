--liquibase formatted sql

--changeset dreamscale:5

create view recent_all_task_view as
 select t.id, t.name, t.lowercase_name, t.description, t.organization_id, t.project_id, rt.member_id, p.is_private, rt.last_accessed
 from task t, project p, recent_task rt
 where t.id = rt.task_id
 and p.id = rt.project_id
 union all
  select pt.id, pt.name, pt.lowercase_name, pt.description, pt.organization_id, pt.project_id, pt.member_id, true as is_private, rt2.last_accessed
 from private_task pt, recent_task rt2
 where pt.id = rt2.task_id
 and pt.member_id = rt2.member_id
 ;

