--liquibase formatted sql

--changeset dreamscale:4
alter table grid_box_metrics
    drop column team_id;