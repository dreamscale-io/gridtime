--liquibase formatted sql

--changeset dreamscale:4

-- how should I manifest

create table gridtime_system_job_claim (
   id uuid primary key not null,
   job_type text,
   job_descriptor_json text,
   started_on timestamp ,
   finished_on timestamp,
   last_heartbeat timestamp ,
   error_message text ,
   job_status text ,
   claiming_worker_id uuid
);

create table gridtime_system_running_job (
   job_id uuid primary key not null,
   job_type text,
   job_params text,
   started_on timestamp ,
   last_heartbeat timestamp ,
   last_exit_status text ,
   run_status text ,
   claiming_worker_id uuid
);

create table gridtime_job (
   id uuid primary key not null,
   organization_id uuid,
   job_id uuid,
   job_name text,
   job_type text,
   job_config_json text,
   job_owner_type text,
   owner_id uuid,
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
   job_type text,
   job_config_json text,
   started_on timestamp ,
   last_heartbeat timestamp ,
   last_exit_status text ,
   run_status text ,
   claiming_worker_id uuid
 );
