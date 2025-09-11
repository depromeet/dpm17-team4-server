alter table users
    add column if not exists profile_image varchar(500);
