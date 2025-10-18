BEGIN;

-- 새 컬럼 추가
ALTER TABLE toilet_record
    ADD COLUMN activity_date DATE,
    ADD COLUMN activity_time TIME WITHOUT TIME ZONE;

-- 기존 데이터 분리 복사
UPDATE toilet_record
SET
    activity_date = (activity_at AT TIME ZONE 'UTC')::date,
    activity_time = (activity_at AT TIME ZONE 'UTC')::time;

-- null 데이터 검증 (optional, 안전용)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM toilet_record WHERE activity_date IS NULL) THEN
        RAISE EXCEPTION 'activity_date contains NULL values';
    END IF;
END $$;

-- NOT NULL 제약 추가
ALTER TABLE toilet_record
    ALTER COLUMN activity_date SET NOT NULL;

-- 기존 인덱스 삭제 (있다면)
DROP INDEX IF EXISTS idx_toilet_record_activity_at;

-- 기존 컬럼 삭제
ALTER TABLE toilet_record
    DROP COLUMN activity_at;

-- 활성 레코드(is_deleted=false) 중복 확인
DO $$
BEGIN
    IF EXISTS (
        SELECT user_id, activity_date, COUNT(*)
        FROM toilet_record
        WHERE is_deleted = false
        GROUP BY user_id, activity_date
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate (user_id, activity_date) detected for active records';
    END IF;
END $$;

-- Partial Unique Index: is_deleted=false인 레코드에만 unique 제약 적용
CREATE UNIQUE INDEX uk_toilet_user_date ON toilet_record (user_id, activity_date)
WHERE is_deleted = false;

COMMIT;
