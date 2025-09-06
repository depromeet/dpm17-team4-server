-- Add OAuth provider fields to user table
alter table app_user
    add column if not exists profile_image varchar(500);

alter table app_user
    add column if not exists provider varchar(50);

alter table app_user
    add column if not exists external_id varchar(255);

alter table app_user
    add column if not exists created_at timestamp;
