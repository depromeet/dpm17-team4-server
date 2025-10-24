create table if not exists toilet_score (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id bigint not null,
    score integer not null,
    date date not null
);

alter table toilet_score add constraint uk_toilet_score_user_date unique (user_id, date);
