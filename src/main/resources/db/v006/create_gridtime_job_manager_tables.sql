--liquibase formatted sql

--changeset dreamscale:4

create table gridtime_job (
   id uuid primary key not null,
   organization_id uuid,
   job_id uuid,
   job_name text,
   job_type text,
   job_config_json text,
   started_on timestamp ,
   last_heartbeat timestamp,
   last_exit_status text ,
   run_status text ,
   claiming_worker_id uuid
 );

 create table gridtime_system_job (
   id uuid primary key not null,
   job_id uuid,
   job_name text,
   system_job_type text,
   job_config_json text,
   started_on timestamp ,
   last_heartbeat timestamp ,
   last_exit_status text ,
   run_status text ,
   claiming_worker_id uuid
 );
