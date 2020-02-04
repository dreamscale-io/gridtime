--liquibase formatted sql

--changeset dreamscale:4

create view team_circuit_talk_room_view as
  select  tcr.id,  tcr.organization_id, t.id team_id, t.name team_name, tcr.local_name circuit_room_name,
  tr.room_name talk_room_name, tr.id talk_room_id, tcr.owner_id, tcr.moderator_id, tcr.description, tcr.json_tags,
  tcr.open_time, tcr.close_time, tcr.circuit_status
  from team_circuit_room tcr, team t, talk_room tr
  where tcr.team_id = t.id
  and tcr.talk_room_id = tr.id;