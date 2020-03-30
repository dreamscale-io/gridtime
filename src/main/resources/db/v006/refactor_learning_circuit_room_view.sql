--liquibase formatted sql

--changeset dreamscale:4

drop view learning_circuit_room_view;

create view learning_circuit_room_view as
  select  tr.id room_id, tr.room_name, lc.id circuit_id, lc.circuit_name, lc.owner_id circuit_owner_id, lc.moderator_id circuit_moderator_id,
          lc.organization_id, lc.circuit_state
  from learning_circuit lc, talk_room tr
  where (lc.wtf_room_id = tr.id or lc.retro_room_id = tr.id or lc.status_room_id = tr.id);
