ALTER TABLE users ADD COLUMN is_deleted BOOLEAN DEFAULT false;
UPDATE users SET is_deleted = false WHERE is_deleted IS NULL;
ALTER TABLE users ALTER COLUMN is_deleted SET NOT NULL;
