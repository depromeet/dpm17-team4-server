-- 1) 오타 컬럼(is_toliet_success) → is_toilet_success (있을 때만)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'toilet_record'
      AND column_name = 'is_toliet_success'
  ) THEN
ALTER TABLE toilet_record RENAME COLUMN is_toliet_success TO is_toilet_success;
END IF;
END$$;

-- 2) 배변 모양 컬럼 추가 (없으면 추가)
ALTER TABLE IF EXISTS toilet_record
    ADD COLUMN IF NOT EXISTS toilet_shape varchar(50);

-- 3) foods.food_score 타입을 double precision으로 변경
ALTER TABLE IF EXISTS foods
ALTER COLUMN food_score TYPE double precision
  USING food_score::double precision;
