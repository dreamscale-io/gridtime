--liquibase formatted sql

--changeset dreamscale:5

drop view wtf_feed_message_view;

create view wtf_feed_message_view as
 select trm.id message_id, c.id circuit_id, tr.id room_id, c.circuit_name, trm.from_id,
        md.display_name from_display_name, md.full_name from_full_name, md.username from_username,
      trm.position, trm.message_type, json_body
 from learning_circuit c,
      talk_room tr,
      talk_room_message trm,
      member_details_view md
   where c.wtf_room_id = tr.id and tr.id = trm.to_room_id and trm.from_id = md.member_id;

drop view learning_circuit_room_view;

create view learning_circuit_room_view as
  select  tr.id room_id, tr.room_name, lc.id circuit_id, lc.circuit_name, lc.owner_id circuit_owner_id, lc.moderator_id circuit_moderator_id,
          lc.organization_id, lc.circuit_state
  from learning_circuit lc, talk_room tr
  where (lc.wtf_room_id = tr.id or lc.retro_room_id = tr.id);

alter table learning_circuit
    drop column status_room_id;
