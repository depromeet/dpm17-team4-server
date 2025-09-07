alter table users
    add constraint uk_user_email unique (email);
    add constraint uk_user_nickname unique (nickname);
