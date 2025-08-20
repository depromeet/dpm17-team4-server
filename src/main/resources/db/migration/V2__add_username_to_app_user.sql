-- Add username column to user table
alter table app_user
    add column if not exists username varchar(255);
