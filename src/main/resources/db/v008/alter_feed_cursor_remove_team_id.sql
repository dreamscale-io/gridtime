--liquibase formatted sql

--changeset dreamscale:5

alter table torchie_feed_cursor drop column team_id;

alter table grid_feature rename column team_id to organization_id;

alter table grid_box_bucket_config rename column team_id to organization_id;


