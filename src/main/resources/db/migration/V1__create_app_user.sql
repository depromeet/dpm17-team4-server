-- Create initial user table with id and email only
create table if not exists app_user (
    id bigserial primary key,
    email varchar(255) not null unique,
    created_at timestamp with time zone default now() not null
);
