-- Add usage_count column with default value 0
ALTER TABLE foods ADD COLUMN usage_count BIGINT NOT NULL DEFAULT 0;

-- Add unique constraint on name column
ALTER TABLE foods ADD CONSTRAINT uk_foods_name UNIQUE (name);

-- Add standard B-tree index for prefix searches (name LIKE 'prefix%')
-- Also useful for exact name lookups and other name-based operations
CREATE INDEX idx_foods_name ON foods(name);

-- Add trigram index for partial string matching (name ILIKE '%substring%')
-- Requires pg_trgm extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_foods_name_trgm ON foods USING gin(name gin_trgm_ops);

-- Add index on usage_count for efficient sorting by popularity
-- Useful for queries that only sort by usage_count without name filtering
CREATE INDEX idx_foods_usage_count ON foods(usage_count DESC);

-- Composite index optimized for the main search query pattern:
-- WHERE name ILIKE '%pattern%' ORDER BY usage_count DESC, name
-- 
-- Order: usage_count DESC first (primary sort), then name (secondary sort)
-- This provides the most efficient sorting for search results
CREATE INDEX idx_foods_usage_count_name ON foods(usage_count DESC, name);

-- Initialize usage_count with random values for testing (optional)
-- You can remove this if you want all foods to start with 0 usage
-- UPDATE foods SET usage_count = FLOOR(RANDOM() * 100) WHERE usage_count = 0;
