--liquibase formatted sql

--changeset dreamscale:2
ALTER TABLE active_spirit_link ADD CONSTRAINT unique_spirit_id UNIQUE (spirit_id);