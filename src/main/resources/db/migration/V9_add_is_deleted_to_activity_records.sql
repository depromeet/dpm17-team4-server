alter table activity_record
    add column is_deleted boolean not null default false;
