--liquibase formatted sql

--changeset dreamscale:4

create table learning_circuit_participant (
 id uuid primary key not null,
 circuit_id uuid,
 organization_id uuid,
 member_id uuid,
 unique (circuit_id, member_id)
);

alter table talk_room_member
 drop column last_active;

alter table talk_room_member
 drop column room_status;


delete from talk_room where room_type = 'WTF_ROOM';

delete from talk_room_member rm where exists (select 1 from talk_room r where r.id = rm.room_id and r.room_type = 'WTF_ROOM');

truncate table talk_room_message;

truncate table learning_circuit;

truncate table learning_circuit_participant;

drop table learning_circuit;

create table learning_circuit (
 id uuid primary key not null,
 organization_id uuid,
 owner_id uuid,
 moderator_id uuid,
 circuit_name text,
 wtf_room_id uuid,
 retro_room_id uuid,
 open_time timestamp,
 circuit_status text,
 total_circuit_elapsed_nano_time bigint,
 total_circuit_paused_nano_time bigint,
 wtf_open_nano_time bigint,
 retro_open_nano_time bigint,
 solved_circuit_nano_time bigint,
 close_circuit_nano_time bigint,
 pause_circuit_nano_time bigint,
 resume_circuit_nano_time bigint,
 cancel_circuit_nano_time bigint,
 unique (organization_id, circuit_name)
);

--we need to remove refs to this first
--drop view member_details_view;

drop view wtf_member_status_view;

drop view retro_member_status_view;

drop view circuit_member_status_view;

create view member_status_view as
select om.organization_id, om.id member_id, om.username, r.full_name, r.display_name, a.last_activity, a.online_status
 from organization_member om, root_account r, active_account_status a
 where om.root_account_id = r.id and r.id = a.root_account_id;

create view room_member_status_view as
select rm.id, rm.room_id, rm.organization_id, rm.member_id,
       ms.username, ms.full_name, ms.display_name, ms.last_activity, ms.online_status
from talk_room_member rm
left join member_status_view ms on rm.member_id = ms.member_id;

create view circuit_member_status_view as
select p.id, p.circuit_id, rm.organization_id, rm.member_id,
       ms.username, ms.full_name, ms.display_name, ms.last_activity, ms.online_status
from learning_circuit_participant p
left join member_status_view ms on p.member_id = ms.member_id;



drop view wtf_member_status_view;

drop view retro_member_status_view;

drop view circuit_member_status_view;

