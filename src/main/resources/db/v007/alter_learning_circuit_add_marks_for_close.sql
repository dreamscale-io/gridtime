--liquibase formatted sql

--changeset dreamscale:5

alter table learning_circuit
    add column marks_for_close integer ;

alter table circuit_mark
   drop constraint circuit_mark_unique_key;

alter table circuit_mark
   add constraint circuit_mark_unique_key unique (organization_id, member_id, circuit_id, mark_type);


alter table learning_circuit_member
   add column join_state text;


alter table learning_circuit
    add column marks_required_for_review integer ;

alter table learning_circuit
    add column marks_required_for_close integer ;
