--liquibase formatted sql

--changeset dreamscale:4
alter table grid_metrics_idea_flow
    drop column last_idea_flow_state;