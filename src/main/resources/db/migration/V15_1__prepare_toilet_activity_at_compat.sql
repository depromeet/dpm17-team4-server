-- 1) activity_at 컬럼이 없으면 추가
ALTER TABLE toilet_record
    ADD COLUMN IF NOT EXISTS activity_at TIMESTAMP WITH TIME ZONE;

-- 2) occurred_at이 있으면 activity_at에 백필 (NULL만)
UPDATE toilet_record
SET activity_at = occurred_at
WHERE activity_at IS NULL;
