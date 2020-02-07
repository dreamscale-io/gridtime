--liquibase formatted sql

--changeset dreamscale:4

alter table master_account
    rename column short_name to display_name;

alter table organization_member
  add column username text;

update organization_member
set username = (select lower(substring(full_name, 1, position(' ' in full_name)))
from master_account m where m.id = master_account_id);

alter table master_account rename to root_account;

alter table root_account
rename column master_email to root_email;

alter table active_user_context
rename column master_account_id to root_account_id;

alter table active_account_status
rename column master_account_id to root_account_id;

alter table organization_member
rename column  master_account_id to root_account_id;
