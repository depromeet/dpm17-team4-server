#!/bin/bash
set -e

TAGS_FILE="./data/tags.csv"
FOOD_TAG_FILE="./data/food_tag.csv"
CONTAINER_NAME="postgres"
HOST_DATA_DIR="./postgres_data"
CONTAINER_DATA_DIR="/var/lib/postgresql/data"
CONTAINER_TAGS_FILE="$CONTAINER_DATA_DIR/tags.csv"
CONTAINER_FOOD_TAG_FILE="$CONTAINER_DATA_DIR/food_tag.csv"
DB_USERNAME="admin"
DB_NAME="dpm"

echo "Pushing tags data to database..."

# Copy CSV files to postgres data directory
cp $TAGS_FILE $HOST_DATA_DIR
cp $FOOD_TAG_FILE $HOST_DATA_DIR

# Insert tags and food_tag data into PostgreSQL
docker exec -i $CONTAINER_NAME psql -U $DB_USERNAME -d $DB_NAME <<EOF
-- Insert tags data
CREATE TEMP TABLE tmp_tags (
    id BIGINT,
    name VARCHAR(255),
    category VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

\copy tmp_tags(id, name, category, description, created_at, updated_at) FROM '$CONTAINER_TAGS_FILE' WITH (FORMAT csv, HEADER true);

INSERT INTO tags (id, name, category, description, created_at, updated_at)
SELECT id, name, category, description, created_at, updated_at FROM tmp_tags
ON CONFLICT (name) DO UPDATE SET
    category = EXCLUDED.category,
    description = EXCLUDED.description,
    updated_at = EXCLUDED.updated_at;

-- Update sequence to avoid ID conflicts
SELECT setval('tags_id_seq', (SELECT MAX(id) FROM tags));

TRUNCATE TABLE tmp_tags;

-- Insert food_tag data
CREATE TEMP TABLE tmp_food_tag (
    id BIGINT,
    food_id BIGINT,
    tag_id BIGINT,
    confidence DOUBLE PRECISION,
    source VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

\copy tmp_food_tag(id, food_id, tag_id, confidence, source, created_at, updated_at) FROM '$CONTAINER_FOOD_TAG_FILE' WITH (FORMAT csv, HEADER true);

INSERT INTO food_tag (id, food_id, tag_id, confidence, source, created_at, updated_at)
SELECT id, food_id, tag_id, confidence, source, created_at, updated_at FROM tmp_food_tag
ON CONFLICT (food_id, tag_id) DO UPDATE SET
    confidence = EXCLUDED.confidence,
    source = EXCLUDED.source,
    updated_at = EXCLUDED.updated_at;

-- Update sequence to avoid ID conflicts
SELECT setval('food_tag_id_seq', (SELECT MAX(id) FROM food_tag));

TRUNCATE TABLE tmp_food_tag;
EOF

# Clean up copied files
rm $HOST_DATA_DIR/tags.csv
rm $HOST_DATA_DIR/food_tag.csv

echo "Tags and food_tag data loaded successfully."