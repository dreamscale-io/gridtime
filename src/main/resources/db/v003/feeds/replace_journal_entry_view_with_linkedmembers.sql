--liquibase formatted sql

--changeset dreamscale:3

drop view journal_entry_view;

create view journal_entry_view as
  select i.id, i.position, i.description, i.project_id, i.task_id, i.organization_id, i.member_id, i.linked,
         i.flame_rating, i.finish_status, i.finish_time, null as circle_id, 'Intention' as journal_entry_type,
         t.name task_name, t.summary task_summary, p.name project_name, jle.linked_members
  from intention i
         join task t on i.task_id = t.id
         join project p on i.project_id = p.id
         left outer join journal_link_event jle on jle.intention_id = i.id

