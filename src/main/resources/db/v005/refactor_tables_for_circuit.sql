--liquibase formatted sql

--changeset dreamscale:4
drop view circle_feed_message_view;
drop table circle_context;
drop table circle_member;
drop table circle_message;
drop table circle;

drop table realtime_channel;
drop table realtime_channel_member;
drop table realtime_channel_message;


create table learning_circuit (
 id uuid primary key not null,
 organization_id uuid,
 owner_id  uuid,
 moderator_id uuid,
 circuit_name text,
 wtf_room_id uuid,
 retro_room_id uuid,
 open_time timestamp,
 close_time timestamp,
 circuit_status text,
 last_on_hold_time timestamp,
 last_resume_time timestamp,
 seconds_before_on_hold bigint,
 unique (organization_id, circuit_name)
);


create table team_learning_circuit (
 id uuid primary key not null,
 organization_id uuid,
 owner_id  uuid,
 moderated_id uuid,
 team_id  uuid,
 circuit_name text
);

create table team_circuit_input_circuit_event (
  id uuid primary key not null,
  organization_id uuid,
  input_circuit_id uuid,
  activation_time timestamp
);


-- GT server side routing tables for messaging

create table talk_room (
 id uuid primary key not null,
 circuit_id uuid,
 talk_room_id text unique,
 organization_id uuid,
 owner_id uuid,
 room_type text
);

create table talk_room_member (
 id uuid primary key not null,
 room_id uuid,
 join_time timestamp,
 organization_id uuid,
 member_id uuid,
 last_active timestamp,
 room_status text,
 unique (room_id, member_id)
);

create table talk_room_message (
 id uuid primary key not null,
from_id uuid,
to_room_id uuid,
position timestamp,
message_type text,
json_body text
);

create table talk_direct_message (
 id uuid primary key not null,
 from_id uuid,
 to_id uuid,
 position timestamp,
 message_type text,
 json_body text
);


create view member_details_view as
 select om.organization_id, om.id member_id, m.short_name, m.full_name
 from organization_member om, master_account m
 where om.master_account_id = m.id;

create view wtf_member_status_view as
select wtf_m.id, c.id circuit_id, c.circuit_name, c.owner_id, wtf_m.organization_id, wtf_m.member_id,
       wtf_m.join_time, wtf_m.last_active, wtf_m.room_status,
       wtf_mdv.short_name, wtf_mdv.full_name
from learning_circuit c
       left join talk_room wtf_tr on c.wtf_room_id = wtf_tr.id
       left join talk_room_member wtf_m on wtf_tr.id = wtf_m.room_id
       left join member_details_view wtf_mdv on wtf_m.member_id = wtf_mdv.member_id;

create view retro_member_status_view as
select retro_m.id, c.id circuit_id, c.circuit_name, c.owner_id, retro_m.organization_id, retro_m.member_id,
       retro_m.join_time, retro_m.last_active, retro_m.room_status,
       retro_mdv.short_name, retro_mdv.full_name
from learning_circuit c
       left join talk_room retro_tr on c.retro_room_id = retro_tr.id
       left join talk_room_member retro_m on retro_tr.id = retro_m.room_id
       left join member_details_view retro_mdv on retro_m.member_id = retro_mdv.member_id;

create view circuit_member_status_view as
select coalesce(w.id, r.id) id,
       coalesce(w.circuit_id, r.circuit_id) circuit_id,
       coalesce(w.circuit_name, r.circuit_name) circuit_name,
       coalesce(w.owner_id, r.owner_id) owner_id,
       coalesce(w.organization_id, r.organization_id) organization_id,
       coalesce(w.member_id, r.member_id) member_id,
       w.join_time wtf_join_time,
       w.last_active wtf_last_active,
       w.room_status wtf_room_status,
       r.join_time retro_join_time,
       r.last_active retro_last_active,
       r.room_status retro_room_status,
       coalesce(w.short_name, r.short_name) short_name,
       coalesce(w.full_name, r.full_name) full_name
from wtf_member_status_view w full outer join retro_member_status_view r
 on w.circuit_id = r.circuit_id
 and w.member_id = r.member_id;


create view wtf_feed_message_view as
 select trm.id message_id, c.id circuit_id, tr.id room_id, c.circuit_name, trm.from_id,
        md.short_name from_short_name, md.full_name from_full_name,
      trm.position, trm.message_type, json_body
 from learning_circuit c,
      talk_room tr,
      talk_room_message trm,
      member_details_view md
   where c.wtf_room_id = tr.id and tr.id = trm.to_room_id and trm.from_id = md.member_id;


create view retro_feed_message_view as
 select trm.id message_id, c.id circuit_id, tr.id room_id, c.circuit_name, trm.from_id,
        md.short_name from_short_name, md.full_name from_full_name,
        trm.position, trm.message_type, json_body
 from learning_circuit c,
      talk_room tr,
      talk_room_message trm,
      member_details_view md
 where c.retro_room_id = tr.id and tr.id = trm.to_room_id and trm.from_id = md.member_id;

