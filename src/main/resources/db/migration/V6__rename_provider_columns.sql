-- Rename provider columns to provider_type and provider_id
-- Adjust for your DB: below works for PostgreSQL

-- If columns already exist, skip; otherwise rename
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'users' AND column_name = 'provider'
  ) THEN
    EXECUTE 'ALTER TABLE users RENAME COLUMN provider TO provider_type';
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'users' AND column_name = 'provider_user_id'
  ) THEN
    EXECUTE 'ALTER TABLE users RENAME COLUMN provider_user_id TO provider_id';
  END IF;
END $$;
