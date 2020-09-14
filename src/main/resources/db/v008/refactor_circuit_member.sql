--liquibase formatted sql

--changeset dreamscale:5

create table terminal_circuit (
 id uuid primary key not null,
 organization_id uuid,
 creator_id uuid unique,
 talk_room_id uuid,
 circuit_name text,
 created_date timestamp,
 constraint terminal_name_unique_key unique(organization_id, circuit_name)
);

drop view circuit_member_status_view;

alter table learning_circuit_member rename to circuit_member;

create view circuit_member_status_view as
select cm.id, cm.circuit_id, cm.organization_id, cm.member_id,
os.username, os.full_name, os.display_name, os.last_activity, os.online_status
from circuit_member cm left join online_status_view os on cm.member_id = os.member_id;
