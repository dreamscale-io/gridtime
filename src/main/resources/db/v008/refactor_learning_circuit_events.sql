--liquibase formatted sql

--changeset dreamscale:5

create table learning_circuit_event (
  id uuid primary key not null,
  circuit_id uuid,
  organization_id uuid,
  from_member_id uuid,
  circuit_message_type text,
  position timestamp,
  nano_time bigint
);

drop view wtf_feed_message_view;

-- learning circuits owned by me, learning circuit events...

create view wtf_feed_message_view as
 select e.id , e.organization_id, e.circuit_id, c.circuit_name, e.from_member_id,
        md.display_name from_display_name, md.full_name from_full_name, md.username from_username,
      e.position, e.circuit_message_type
 from learning_circuit c,
      learning_circuit_event e,
      member_details_view md
   where c.id = e.circuit_id and e.from_member_id = md.member_id;

drop view learning_circuit_room_view;

delete from talk_room_message where message_type = 'RoomMemberJoinEventDto';
delete from talk_room_message where message_type = 'RoomMemberLeaveEventDto';

