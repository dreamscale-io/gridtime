--liquibase formatted sql

--changeset dreamscale:4

delete from talk_room where room_type = 'WTF_ROOM';

delete from talk_room_member rm where exists (select 1 from talk_room r where r.id = rm.room_id and r.room_type = 'WTF_ROOM');

truncate table talk_room_message;

truncate table learning_circuit;

