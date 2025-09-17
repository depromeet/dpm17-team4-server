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

 -- 3-a) FK 매핑용 신규 컬럼 추가
 alter table food_record add column food_id_new bigint;
 alter table food_record add column activity_record_id_new bigint;
 alter table activity_record add column user_id_new bigint;
 alter table toilet_record add column user_id_new bigint;

 -- 3-b) 참조 매핑 (기존 UUID → 대상 id_new)
 update food_record fr
         set food_id_new = f.id_new
        from foods f
       where fr.food_id = f.id;
 update food_record fr
         set activity_record_id_new = ar.id_new
        from activity_record ar
       where fr.activity_record_id = ar.id;
 update activity_record ar
         set user_id_new = u.id_new
        from users u
       where ar.user_id = u.id;
 update toilet_record tr
         set user_id_new = u.id_new
        from users u
       where tr.user_id = u.id;

 -- 3-c) 교체
 alter table food_record drop column food_id;
 alter table food_record rename column food_id_new to food_id;
 alter table food_record drop column activity_record_id;
 alter table food_record rename column activity_record_id_new to activity_record_id;
 alter table activity_record drop column user_id;
 alter table activity_record rename column user_id_new to user_id;
 alter table toilet_record drop column user_id;
 alter table toilet_record rename column user_id_new to user_id;

-- 4) 기존 PK 드롭
alter table foods           drop constraint foods_pkey;
alter table food_record     drop constraint food_record_pkey;
alter table activity_record drop constraint activity_record_pkey;
alter table users           drop constraint users_pkey;

-- 5) 새 PK 추가 (default naming 유지)
-- (선행) 기존 행에 id_new 백필
update foods set id_new = default where id_new is null;
update food_record set id_new = default where id_new is null;
update activity_record set id_new = default where id_new is null;
update users set id_new = default where id_new is null;

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
