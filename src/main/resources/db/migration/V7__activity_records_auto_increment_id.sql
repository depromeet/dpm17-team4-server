-- 1) 새 auto-increment 컬럼 추가
alter table activity_record add column id_new bigint generated always as identity;
alter table food_record     add column id_new bigint generated always as identity;
alter table foods           add column id_new bigint generated always as identity;
alter table users           add column id_new bigint generated always as identity;

-- 2) FK 드롭 (food_record, activity_record, toilet_record)
alter table food_record   drop constraint fk_food_record_food;
alter table food_record   drop constraint fk_food_record_activity;
alter table activity_record drop constraint fk_activity_record_user;
alter table toilet_record  drop constraint fk_toilet_record_user;

-- 3) FK 컬럼 타입 변환: uuid → bigint
alter table food_record alter column food_id type bigint using null;
alter table food_record alter column activity_record_id type bigint using null;
alter table activity_record alter column user_id type bigint using null;
alter table toilet_record alter column user_id type bigint using null;

-- 4) 기존 PK 드롭
alter table foods           drop constraint foods_pkey;
alter table food_record     drop constraint food_record_pkey;
alter table activity_record drop constraint activity_record_pkey;
alter table users           drop constraint users_pkey;

-- 5) 새 PK 추가 (default naming 유지)
alter table foods           add constraint foods_pkey primary key (id_new);
alter table food_record     add constraint food_record_pkey primary key (id_new);
alter table activity_record add constraint activity_record_pkey primary key (id_new);
alter table users           add constraint users_pkey primary key (id_new);

-- 6) FK 재생성
alter table food_record
    add constraint fk_food_record_activity
        foreign key (activity_record_id) references activity_record (id_new) on delete restrict;

alter table food_record
    add constraint fk_food_record_food
        foreign key (food_id) references foods (id_new) on delete restrict;

alter table activity_record
    add constraint fk_activity_record_user
        foreign key (user_id) references users (id_new) on delete restrict;

alter table toilet_record
    add constraint fk_toilet_record_user
        foreign key (user_id) references users (id_new) on delete restrict;

-- 7) 기존 uuid PK 컬럼 제거
alter table activity_record drop column id;
alter table food_record     drop column id;
alter table foods           drop column id;
alter table users           drop column id;

-- 8) 새 컬럼 이름 변경
alter table foods           rename column id_new to id;
alter table food_record     rename column id_new to id;
alter table activity_record rename column id_new to id;
alter table users           rename column id_new to id;
