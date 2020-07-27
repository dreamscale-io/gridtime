--liquibase formatted sql

--changeset dreamscale:7

delete from intention where id in (select intention_id from wtf_journal_link);

drop table wtf_journal_link;

