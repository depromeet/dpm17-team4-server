#!/bin/bash
set -e

DATA_FILE="./data/result.txt"
CONTAINER_NAME="postgres"
HOST_DATA_DIR="./postgres_data"
CONTAINER_DATA_DIR="/var/lib/postgresql/data"
CONTAINER_DATA_FILE="$CONTAINER_DATA_DIR/result.txt"
DB_USERNAME="admin"
DB_NAME="dpm"

echo "Pushing data to foods table..."

cp $DATA_FILE $HOST_DATA_DIR

# Postgres 컨테이너 안에서 CSV를 COPY로 삽입
docker exec -i $CONTAINER_NAME psql -U $DB_USERNAME -d $DB_NAME <<EOF
CREATE TEMP TABLE tmp_foods (
    name TEXT,
    m NUMERIC,
    s NUMERIC
);
\copy tmp_foods(name, m, s) FROM '$CONTAINER_DATA_FILE' WITH (FORMAT csv, HEADER true);
INSERT INTO foods (name, score, created_at, updated_at)
SELECT name, m * 20, NOW(), NOW() FROM tmp_foods;
TRUNCATE TABLE tmp_foods;
EOF

rm $HOST_DATA_DIR/result.txt

echo "CSV data loaded into foods table."
