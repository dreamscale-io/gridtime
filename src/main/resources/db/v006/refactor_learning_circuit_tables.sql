--liquibase formatted sql

--changeset dreamscale:4

create table learning_circuit_member (
 id uuid primary key not null,
 circuit_id uuid,
 organization_id uuid,
 member_id uuid,
 join_time timestamp,
 unique (circuit_id, member_id)
);

drop view circuit_member_status_view;

drop view wtf_member_status_view;

drop view retro_member_status_view;

drop view wtf_feed_message_view;

drop view retro_feed_message_view;

drop view team_circuit_talk_room_view;

drop view learning_circuit_room_view;

drop view member_details_view;

alter table talk_room_member
 drop column last_active;

alter table talk_room_member
 drop column room_status;

alter table team_circuit_room
 rename column circuit_status to circuit_state;

drop table learning_circuit;

create table learning_circuit (
 id uuid primary key not null,
 organization_id uuid,
 owner_id uuid,
 moderator_id uuid,
 circuit_name text,
  json_tags text,
 description text,
 wtf_room_id uuid,
 retro_room_id uuid,
 status_room_id uuid,
 open_time timestamp,
 circuit_state text,
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

create view online_status_view as
select om.organization_id, om.id member_id, om.username, r.full_name, r.display_name, a.last_activity, a.online_status
 from organization_member om join root_account r on om.root_account_id = r.id
  left join active_account_status a on r.id = a.root_account_id;

create view room_member_status_view as
select rm.id, rm.room_id, rm.organization_id, rm.member_id,
os.username, os.full_name, os.display_name, os.last_activity, os.online_status
from talk_room_member rm left join online_status_view os on rm.member_id = os.member_id;

create view circuit_member_status_view as
select lcm.id, lcm.circuit_id, lcm.organization_id, lcm.member_id,
os.username, os.full_name, os.display_name, os.last_activity, os.online_status
from learning_circuit_member lcm left join online_status_view os on lcm.member_id = os.member_id;

create view member_details_view as
 select om.organization_id, om.id member_id, om.username, r.display_name, r.full_name
 from organization_member om, root_account r
 where om.root_account_id = r.id;

create view wtf_feed_message_view as
 select trm.id message_id, c.id circuit_id, tr.id room_id, c.circuit_name, trm.from_id,
        md.display_name from_display_name, md.full_name from_full_name, md.username from_username,
      trm.position, trm.message_type, json_body
 from learning_circuit c,
      talk_room tr,
      talk_room_message trm,
      member_details_view md
   where c.status_room_id = tr.id and tr.id = trm.to_room_id and trm.from_id = md.member_id;


create view team_circuit_talk_room_view as
  select  tcr.id,  tcr.organization_id, t.id team_id, t.name team_name, tcr.local_name circuit_room_name,
  tr.room_name talk_room_name, tr.id talk_room_id, tcr.owner_id,  m1.full_name owner_name, m1.username owner_username, m1.display_name owner_display_name,
  tcr.moderator_id, m2.full_name moderator_name, m2.username moderator_username, m2.display_name moderator_display_name,
  tcr.description, tcr.json_tags,
  tcr.open_time, tcr.close_time, tcr.circuit_state
  from team_circuit_room tcr, team t, talk_room tr, member_details_view m1, member_details_view m2
  where tcr.team_id = t.id
  and tcr.talk_room_id = tr.id
  and tcr.owner_id = m1.member_id
  and tcr.moderator_id = m2.member_id;

create view learning_circuit_room_view as
  select  tr.id room_id, tr.room_name, lc.id circuit_id, lc.circuit_name, lc.owner_id circuit_owner_id, lc.moderator_id circuit_moderator_id,
          lc.organization_id, lc.circuit_state
  from learning_circuit lc, talk_room tr
  where (lc.wtf_room_id = tr.id or lc.retro_room_id = tr.id);
