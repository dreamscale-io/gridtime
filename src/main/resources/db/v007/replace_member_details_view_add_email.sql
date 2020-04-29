--liquibase formatted sql

--changeset dreamscale:5

drop view wtf_feed_message_view;

drop view team_circuit_talk_room_view;

drop view member_details_view;

create view member_details_view as
 select om.organization_id, om.id member_id, om.username, om.email, r.display_name, r.full_name
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
