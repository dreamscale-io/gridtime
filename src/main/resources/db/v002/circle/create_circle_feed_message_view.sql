--liquibase formatted sql

--changeset dreamscale:2
create view circle_feed_message_view as
select cf.id, cf.circle_id, om.id member_id, m.full_name, cf.time_position, cf.message_type, cf.metadata
from circle_feed cf
join organization_member om on cf.member_id = om.id
join master_account m on om.master_account_id = m.id;
