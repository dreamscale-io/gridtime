--liquibase formatted sql

--changeset dreamscale:7

delete from talk_room_message where message_type = 'WTFStatusUpdateDto';

