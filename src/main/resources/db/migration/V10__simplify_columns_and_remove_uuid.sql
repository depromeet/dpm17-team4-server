-- Simplify field names in toilet_record table
-- Rename columns to match the updated entity field names

-- Rename isToiletSuccess to isSuccessful
ALTER TABLE toilet_record RENAME COLUMN is_toilet_success TO is_successful;

-- Rename toiletColor to color
ALTER TABLE toilet_record RENAME COLUMN toilet_color TO color;

-- Rename toiletShape to shape
ALTER TABLE toilet_record RENAME COLUMN toilet_shape TO shape;

-- Rename painScore to pain
ALTER TABLE toilet_record RENAME COLUMN pain_score TO pain;

-- Rename toiletDuration to duration
ALTER TABLE toilet_record RENAME COLUMN toilet_duration TO duration;

-- Rename additionalNote to note
ALTER TABLE toilet_record RENAME COLUMN additional_note TO note;

-- Rename toiletAt to occurredAt
ALTER TABLE toilet_record RENAME COLUMN toilet_at TO occurred_at;

-- Change id from UUID to incremental long
-- Step 1: Create sequence manually to have control over the name
CREATE SEQUENCE toilet_record_new_id_seq;

-- Step 2: Add new id_new column with default value from sequence
ALTER TABLE toilet_record ADD COLUMN id_new BIGINT DEFAULT nextval('toilet_record_new_id_seq');

-- Step 3: Populate id_new with sequential numbers for existing records
UPDATE toilet_record SET id_new = nextval('toilet_record_new_id_seq');

-- Step 4: Drop the old primary key constraint
ALTER TABLE toilet_record DROP CONSTRAINT toilet_record_pkey;

-- Step 5: Drop the old UUID id column
ALTER TABLE toilet_record DROP COLUMN id;

-- Step 6: Rename id_new to id
ALTER TABLE toilet_record RENAME COLUMN id_new TO id;

-- Step 7: Add primary key constraint to the new id column
ALTER TABLE toilet_record ADD PRIMARY KEY (id);

-- Step 8: Rename the sequence to match the new column name
ALTER SEQUENCE toilet_record_new_id_seq RENAME TO toilet_record_id_seq;

-- Step 9: Update the sequence to start from the next available number
-- This ensures new records get proper sequential IDs
SELECT setval('toilet_record_id_seq', (SELECT MAX(id) FROM toilet_record));
