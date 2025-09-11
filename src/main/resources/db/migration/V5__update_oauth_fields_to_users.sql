-- Add profile_image column
alter table users
    add column if not exists profile_image varchar(512);

alter table users
    drop constraint if exists uk_user_nickname;

alter table users
    drop constraint if exists users_nickname_key;

alter table users
    drop constraint if exists users_nickname_unique;

alter table users
    alter column nickname drop not null;

alter table users
    alter column nickname type varchar(32);

alter table users rename column provider to provider_type;
alter table users rename column provider_user_id to provider_id;
