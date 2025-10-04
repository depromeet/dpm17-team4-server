alter table activity_record
    add constraint uk_user_date unique (user_id, activity_date);
