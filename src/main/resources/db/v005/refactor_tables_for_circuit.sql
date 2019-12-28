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
 cumulative_seconds_before_on_hold bigint,
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
 talk_room_id text unique,
 organization_id uuid,
 owner_id uuid,
 feed_type text
);

create table talk_room_member (
 id uuid primary key not null,
 room_id uuid,
 join_time timestamp,
 organization_id uuid,
 member_id uuid,
 last_active timestamp,
 active_status text,
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

