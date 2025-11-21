-- Create food_tag junction table
CREATE TABLE food_tag (
    id BIGSERIAL PRIMARY KEY,
    food_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    confidence DOUBLE PRECISION,
    source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_tag_food FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    CONSTRAINT fk_food_tag_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT uk_food_tag_food_tag UNIQUE (food_id, tag_id)
);

-- Add index on food_id for efficient lookups by food
CREATE INDEX idx_food_tag_food_id ON food_tag(food_id);

-- Add index on tag_id for efficient lookups by tag
CREATE INDEX idx_food_tag_tag_id ON food_tag(tag_id);

-- Add index on source for filtering by source type
CREATE INDEX idx_food_tag_source ON food_tag(source);

-- Add constraint check for source values
ALTER TABLE food_tag ADD CONSTRAINT chk_food_tag_source
    CHECK (source IS NULL OR source IN ('LLM', 'RULE', 'MANUAL'));
