--liquibase formatted sql

--changeset dreamscale:5

update organization set org_name = 'Open' where org_name = 'Public';

update organization set domain_name = 'open.dreamscale.io' where domain_name = 'public.dreamscale.io';
