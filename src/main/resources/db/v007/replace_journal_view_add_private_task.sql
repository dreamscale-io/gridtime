--liquibase formatted sql

--changeset dreamscale:5

create view all_task_view as
 select t.id, t.name, t.lowercase_name, t.description, t.organization_id, t.project_id, p.is_private
 from task t, project p
 where p.id = t.project_id
 union all
  select pt.id, pt.name, pt.lowercase_name, pt.description, pt.organization_id, pt.project_id, true as is_private
 from private_task pt
 ;


drop view journal_entry_view;

create view journal_entry_view as
  select i.id, i.position, i.position as created_date, i.description, i.project_id, i.task_id, i.organization_id, i.member_id, mdv.username, i.linked,
         i.flame_rating, i.finish_status, i.finish_time, 'Intention' as journal_entry_type,
         t.name task_name, t.description task_summary, p.name project_name, jle.linked_members
  from intention i
         join all_task_view t on i.task_id = t.id
         join project p on i.project_id = p.id
         left outer join member_details_view mdv on i.member_id = mdv.member_id
         left outer join journal_link_event jle on jle.intention_id = i.id

