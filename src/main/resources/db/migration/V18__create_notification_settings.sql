CREATE TABLE notification_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    `key` VARCHAR(32) NOT NULL,
    registration_token VARCHAR(512) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_notification_settings_user_id_key UNIQUE (user_id, `key`)
);

CREATE INDEX idx_notification_settings_user_id ON notification_settings(user_id);

