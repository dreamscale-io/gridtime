--liquibase formatted sql

--changeset dreamscale:2
drop view member_name_view;

create view member_name_view as
select om.id spirit_id, m.full_name
from organization_member om
join master_account m on om.master_account_id = m.id;

