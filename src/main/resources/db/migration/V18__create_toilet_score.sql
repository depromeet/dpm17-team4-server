create table if not exists toilet_score (
    id bigint primary key,
    user_id bigint,
    score integer not null
)
