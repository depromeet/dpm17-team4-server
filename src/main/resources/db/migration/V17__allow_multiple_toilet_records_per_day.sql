BEGIN;

-- 1) 유니크 인덱스 드랍
DROP INDEX IF EXISTS uk_toilet_user_date;

-- 2) 조회 성능 유지용 비유니크(부분) 인덱스 재생성
CREATE INDEX IF NOT EXISTS idx_toilet_user_date
    ON toilet_record (user_id, activity_date)
    WHERE is_deleted = false;

COMMIT;
