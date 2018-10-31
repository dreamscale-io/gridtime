--liquibase formatted sql

--changeset dreamscale:1
drop view journal_entry_view;

create view journal_entry_view as
select i.*, t.name task_name, t.summary task_summary, p.name project_name
from intention i
join task t on i.task_id = t.id
join project p on i.project_id = p.id;

