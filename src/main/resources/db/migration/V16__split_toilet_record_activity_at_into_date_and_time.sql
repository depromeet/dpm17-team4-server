-- 새 컬럼 추가
ALTER TABLE toilet_record
    ADD COLUMN activity_date DATE,
    ADD COLUMN activity_time TIME WITHOUT TIME ZONE;

-- 기존 데이터 분리 복사
UPDATE toilet_record
SET
    activity_date = (occurred_at AT TIME ZONE 'UTC')::date,
    activity_time = (occurred_at AT TIME ZONE 'UTC')::time;

-- null 데이터 검증 (optional, 안전용)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM toilet_record WHERE activity_date IS NULL) THEN
        RAISE EXCEPTION 'activity_date contains NULL values';
    END IF;
    IF EXISTS (SELECT 1 FROM toilet_record WHERE activity_time IS NULL) THEN
        RAISE EXCEPTION 'activity_time contains NULL values';
    END IF;
END $$;

-- NOT NULL 제약 추가
ALTER TABLE toilet_record
    ALTER COLUMN activity_date SET NOT NULL,
    ALTER COLUMN activity_time SET NOT NULL;

-- 기존 인덱스 삭제 (있다면)
DROP INDEX IF EXISTS idx_toilet_record_toilet_at;

-- 기존 컬럼 삭제
ALTER TABLE toilet_record
    DROP COLUMN occurred_at;
