-- Add gender and birth_year columns to users table
ALTER TABLE users ADD COLUMN gender VARCHAR(1);
ALTER TABLE users ADD COLUMN birth_year INTEGER;
