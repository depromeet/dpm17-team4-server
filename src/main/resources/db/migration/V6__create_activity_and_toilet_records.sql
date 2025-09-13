-- Create ActivityRecord table
create table if not exists activity_record (
    id uuid primary key,
    user_id uuid not null,
    water_intake_cups int,
    stress_level varchar(50),
    activity_at timestamp with time zone not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
                             constraint fk_activity_record_user
                             foreign key (user_id) references users (id) on delete restrict
    );

create index if not exists idx_activity_record_user_id
    on activity_record (user_id);

create index if not exists idx_activity_record_activity_at
    on activity_record (activity_at);

-- Create Food table
create table if not exists foods (
    id uuid primary key,
    name varchar(255) not null,
    food_score double precision not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
                             );

-- Create FoodRecord table
create table if not exists food_record (
    id uuid primary key,
    activity_record_id uuid not null,
    food_id uuid not null,
    meal_time varchar(50) not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
        constraint fk_food_record_activity
        foreign key (activity_record_id) references activity_record (id) on delete cascade,
    constraint fk_food_record_food
    foreign key (food_id) references foods (id) on delete cascade
    );

-- Create ToiletRecord table
create table if not exists toilet_record (
    id uuid primary key,
    user_id uuid not null,
    is_toliet_success boolean,
    toilet_color varchar(50),
    pain_score int,
    toilet_duration int,
    additional_note varchar(500),
    toilet_at timestamp with time zone not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
        constraint fk_toilet_record_user
        foreign key (user_id) references users (id) on delete restrict
    );

create index if not exists idx_toilet_record_user_id
    on toilet_record (user_id);

create index if not exists idx_toilet_record_toilet_at
    on toilet_record (toilet_at);
