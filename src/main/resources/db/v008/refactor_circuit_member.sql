--liquibase formatted sql

--changeset dreamscale:5

drop view circuit_member_status_view;

alter table learning_circuit_member rename to circuit_member;

create view circuit_member_status_view as
select cm.id, cm.circuit_id, cm.organization_id, cm.member_id, cm.is_active_in_session,
os.username, os.full_name, os.display_name, os.last_activity, os.online_status
from circuit_member cm left join online_status_view os on cm.member_id = os.member_id;
