--liquibase formatted sql

--changeset dreamscale:4

create view circuit_talk_room_view as
  select  tr.id room_id, tr.room_name, lc.id circuit_id, lc.circuit_name, lc.owner_id circuit_owner_id,
          lc.organization_id, lc.circuit_status
  from learning_circuit lc, talk_room tr
  where lc.id = tr.circuit_id;

