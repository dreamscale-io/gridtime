truncate table project cascade;
truncate table config_project_sync cascade;
truncate table task cascade;
truncate table organization cascade;
truncate table flow_activity cascade;
truncate table flow_event cascade;
truncate table intention cascade;
truncate table recent_project cascade;
truncate table recent_task cascade;
truncate table active_account_status cascade;
truncate table active_work_status cascade;
truncate table root_account cascade;
truncate table organization_member cascade;
truncate table team_member cascade;
truncate table team cascade;

truncate table grid_calendar cascade ;
truncate table talk_room cascade ;
truncate table talk_room_message cascade ;
truncate table talk_room_member cascade ;

truncate table grid_idea_flow_metrics cascade ;
truncate table grid_box_metrics cascade ;
truncate table grid_row;
truncate table gridtime_system_job_claim cascade ;

truncate table learning_circuit;
truncate table team_circuit;
truncate table circuit_member;
truncate table team_circuit_room;

truncate table one_time_ticket;
truncate table active_join_circuit;

truncate table project_grant_access;
truncate table project_grant_tombstone;

truncate table team_member_home;
truncate table torchie_feed_cursor;
truncate table grid_feature;

truncate table terminal_circuit;
truncate table terminal_circuit_command_history;
truncate table terminal_circuit_location_history;

truncate table work_item_to_aggregate ; --this table is deadlocking during the tests, so put at end for now