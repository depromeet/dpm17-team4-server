DROP TABLE IF EXISTS app_user;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    nickname VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255),
    refresh_token TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Add constraints for enum values
ALTER TABLE users ADD CONSTRAINT chk_users_role 
    CHECK (role IN ('USER', 'ADMIN'));

ALTER TABLE users ADD CONSTRAINT chk_users_provider
    CHECK (provider IN ('APPLE', 'KAKAO', 'LOCAL'));
