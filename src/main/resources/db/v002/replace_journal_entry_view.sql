--liquibase formatted sql

--changeset dreamscale:2

drop view journal_entry_view;

create view journal_entry_view as
select i.id, i.position, i.description, i.project_id, i.task_id, i.organization_id, i.member_id,
       i.flame_rating, i.finish_status, i.finish_time, null as circle_id, 'Intention' as journal_entry_type,
       t.name task_name, t.summary task_summary, p.name project_name
from intention i
join task t on i.task_id = t.id
join project p on i.project_id = p.id
UNION ALL
select c.id, c.position, c.description, c.project_id, c.task_id, c.organization_id, c.member_id,
       c.flame_rating, c.finish_status, c.finish_time, c.circle_id, 'WTF' as journal_entry_type,
       t.name task_name, t.summary task_summary, p.name project_name
from circle_context c
       join task t on c.task_id = t.id
       join project p on c.project_id = p.id;
