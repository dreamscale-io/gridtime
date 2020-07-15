--liquibase formatted sql

--changeset dreamscale:5

alter table learning_circuit_member
    add column is_active_in_session boolean;


drop view circuit_member_status_view;

create view circuit_member_status_view as
select lcm.id, lcm.circuit_id, lcm.organization_id, lcm.member_id, lcm.is_active_in_session,
os.username, os.full_name, os.display_name, os.last_activity, os.online_status
from learning_circuit_member lcm left join online_status_view os on lcm.member_id = os.member_id;
