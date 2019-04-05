--liquibase formatted sql

--changeset dreamscale:2
drop view circle_feed_message_view;

create view circle_feed_message_view as
select cm.id, cm.circle_id, c.circle_name, cm.spirit_id, m.full_name, cm.position, cm.message_type, cm.metadata
from circle_message cm
join circle c on cm.circle_id = c.id
join organization_member om on cm.spirit_id = om.id
join master_account m on om.master_account_id = m.id;
