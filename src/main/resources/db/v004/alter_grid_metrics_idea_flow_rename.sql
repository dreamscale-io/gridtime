--liquibase formatted sql

--changeset dreamscale:4
alter table grid_metrics_idea_flow
 rename to grid_idea_flow_metrics;