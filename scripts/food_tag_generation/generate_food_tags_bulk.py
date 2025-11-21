#!/usr/bin/env python3
"""
Food Tag Generation Script (Bulk Processing Version)

This script queries foods from the database in batches, generates tags using Clova Studio LLM,
and inserts them into the database with proper relationships using bulk operations.

Performance improvements:
- Bulk LLM API calls (multiple foods per request)
- Batch database inserts
- Reduced network round trips

Usage:
    python scripts/generate_food_tags_bulk.py [--dry-run] [--limit N] [--batch-size N]
"""

import json
import os
import sys
import argparse
import time
from typing import List, Dict, Any, Optional, Tuple
from pathlib import Path
import psycopg2
from psycopg2.extras import RealDictCursor, execute_batch
import requests
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed
from dotenv import load_dotenv

# Load .env file from project root
project_root = Path(__file__).parent.parent.parent
dotenv_path = project_root / '.env'
load_dotenv(dotenv_path)


# ============================================
# Configuration
# ============================================

DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", "5432")),
    "dbname": os.getenv("DB_NAME", "dpm"),
    "user": os.getenv("DB_USERNAME", "admin"),
    "password": os.getenv("DB_PASSWORD", "dpm"),
}

CLOVA_CONFIG = {
    "url": "https://clovastudio.stream.ntruss.com/v3/chat-completions/HCX-005",
    "api_key": os.getenv("CLOVA_API_KEY"),
    "max_tokens": 2000,  # Increased for bulk processing (10 foods * ~200 tokens each)
    "temperature": 0.3,
}

SYSTEM_PROMPT = """당신은 음식 정보를 기반으로 표준화된 태그를 생성하는 전문가입니다.

각 음식에 대해 다음 카테고리의 태그를 JSON 객체로 반환하세요:
- FLAVOR: 맛 특성 (SPICY, SWEET, SALTY, SOUR, BITTER, UMAMI, SAVORY)
- COOKING_METHOD: 조리 방법 (FRIED, GRILLED, BOILED, STEAMED, RAW, BAKED)
- INGREDIENT: 주요 재료 (MEAT, SEAFOOD, VEGETABLE, DAIRY, GRAIN, FRUIT)
- NUTRIENT: 영양 특성 (HIGH_PROTEIN, HIGH_CARB, HIGH_FAT, LOW_CALORIE, HIGH_FIBER)
- DIETARY: 식이 제한 (VEGETARIAN, VEGAN, GLUTEN_FREE, LACTOSE_FREE, HALAL)
- ALLERGEN: 알레르기 유발 물질 (DAIRY, GLUTEN, SHELLFISH, NUTS, EGGS, SOY)

응답 형식 (각 음식명을 키로 사용):
{
  "음식명1": [
    {"name": "SPICY", "category": "FLAVOR", "description": "매운 맛", "confidence": 0.95},
    {"name": "FRIED", "category": "COOKING_METHOD", "description": "튀김 조리", "confidence": 0.9}
  ],
  "음식명2": [...]
}

반드시 JSON 객체만 출력하고, 코드블록(```json```)을 사용하지 마세요."""


# ============================================
# Database Operations
# ============================================

def get_db_connection():
    """Create database connection"""
    return psycopg2.connect(**DB_CONFIG)


def fetch_all_foods(conn, limit: Optional[int] = None, skip_processed: bool = False) -> List[Dict[str, Any]]:
    """
    Fetch all foods from database

    Args:
        conn: Database connection
        limit: Maximum number of foods to fetch
        skip_processed: If True, only fetch foods without any tags
    """
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        if skip_processed:
            # Only fetch foods that don't have any tags yet
            query = """
                SELECT f.id, f.name, f.score
                FROM foods f
                LEFT JOIN food_tag ft ON f.id = ft.food_id
                WHERE ft.id IS NULL
                ORDER BY f.id
            """
        else:
            query = "SELECT id, name, score FROM foods ORDER BY id"

        if limit:
            query += f" LIMIT {limit}"

        cur.execute(query)
        return cur.fetchall()


def bulk_insert_or_get_tags(conn, tags_data: List[Dict[str, Any]]) -> Dict[str, int]:
    """
    Bulk insert tags and return mapping of tag_name -> tag_id
    Uses INSERT ... ON CONFLICT for efficient upserts
    """
    with conn.cursor() as cur:
        # Prepare unique tags
        unique_tags = {tag["name"]: tag for tag in tags_data}

        if not unique_tags:
            return {}

        # Bulk insert with ON CONFLICT
        insert_values = [
            (
                name,
                tag.get("category"),
                tag.get("description"),
                datetime.now(),
                datetime.now(),
            )
            for name, tag in unique_tags.items()
        ]

        execute_batch(
            cur,
            """
            INSERT INTO tags (name, category, description, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s)
            ON CONFLICT (name) DO NOTHING
            """,
            insert_values,
            page_size=100
        )

        # Fetch all tag IDs
        tag_names = list(unique_tags.keys())
        cur.execute(
            "SELECT id, name FROM tags WHERE name = ANY(%s)",
            (tag_names,)
        )

        return {row[1]: row[0] for row in cur.fetchall()}


def bulk_insert_food_tags(conn, food_tags_data: List[Tuple[int, int, float]]):
    """
    Bulk insert food-tag relationships
    food_tags_data: List of (food_id, tag_id, confidence)
    """
    if not food_tags_data:
        return

    with conn.cursor() as cur:
        insert_values = [
            (food_id, tag_id, confidence, "LLM", datetime.now(), datetime.now())
            for food_id, tag_id, confidence in food_tags_data
        ]

        execute_batch(
            cur,
            """
            INSERT INTO food_tag (food_id, tag_id, confidence, source, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s)
            ON CONFLICT (food_id, tag_id) DO NOTHING
            """,
            insert_values,
            page_size=100
        )


# ============================================
# LLM Integration (Bulk)
# ============================================

def generate_tags_for_foods_bulk(foods: List[Dict[str, Any]], max_retries: int = 3) -> Tuple[Dict[str, List[Dict[str, Any]]], Optional[str]]:
    """
    Generate tags for multiple foods in one API call with retry logic
    Returns: (tags_by_food, error_type)
    error_type: None (success), "token_limit", "api_error", "parse_error", "unknown"
    """
    food_names = [food["name"] for food in foods]
    food_list_text = "\n".join([f"- {name}" for name in food_names])

    user_prompt = f"""다음 음식들에 대해 태그를 생성해주세요:\n\n{food_list_text}"""

    headers = {
        "Authorization": f"Bearer {CLOVA_CONFIG['api_key']}",
        "Content-Type": "application/json",
    }

    body = {
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_prompt}
        ],
        "maxTokens": CLOVA_CONFIG["max_tokens"],
        "temperature": CLOVA_CONFIG["temperature"]
    }

    for attempt in range(max_retries):
        try:
            response = requests.post(
                CLOVA_CONFIG["url"],
                json=body,
                headers=headers,
                timeout=60
            )
            response.raise_for_status()
            data = response.json()

            # Extract content
            content = data["result"]["message"]["content"]

            # Remove markdown code block if present
            if content.startswith("```"):
                content = content.strip("`")
                content = content.replace("json\n", "").replace("json", "")
                content = content.strip()

            # Parse JSON
            tags_by_food = json.loads(content)

            # Validate structure
            if not isinstance(tags_by_food, dict):
                raise ValueError("Response is not a JSON object")

            return tags_by_food, None

        except requests.HTTPError as e:
            # Check for token limit error (429 or specific error messages)
            if e.response.status_code == 429 or "token" in str(e).lower() or "limit" in str(e).lower():
                print(f"  ⚠️  Token limit error detected (attempt {attempt + 1}/{max_retries})")
                if attempt < max_retries - 1:
                    wait_time = 2 ** attempt  # Exponential backoff
                    print(f"  ⏳ Waiting {wait_time}s before retry...")
                    time.sleep(wait_time)
                    continue
                return {}, "token_limit"

            print(f"  ❌ HTTP error: {e} (attempt {attempt + 1}/{max_retries})")
            if attempt < max_retries - 1:
                time.sleep(1)
                continue
            return {}, "api_error"

        except requests.RequestException as e:
            print(f"  ❌ API request failed: {e} (attempt {attempt + 1}/{max_retries})")
            if attempt < max_retries - 1:
                time.sleep(1)
                continue
            return {}, "api_error"

        except json.JSONDecodeError as e:
            print(f"  ❌ JSON parse error: {e}")
            print(f"  Content length: {len(content)} characters")
            print(f"  Raw content (first 500 chars): {content[:500]}...")
            if len(content) > 500:
                print(f"  Raw content (last 500 chars): ...{content[-500:]}")
            # Save full response to file for debugging
            with open("scripts/food_tag_generation/last_failed_response.json", "w") as f:
                f.write(content)
            print(f"  💾 Full response saved to: scripts/food_tag_generation/last_failed_response.json")
            return {}, "parse_error"

        except Exception as e:
            print(f"  ❌ Unexpected error: {e}")
            import traceback
            traceback.print_exc()
            return {}, "unknown"

    return {}, "max_retries_exceeded"


# ============================================
# Batch Processing
# ============================================

def process_food_batch(
    conn,
    foods: List[Dict[str, Any]],
    batch_num: int,
    total_batches: int,
    dry_run: bool = False
) -> Dict[str, Any]:
    """
    Process a batch of foods with error handling
    Returns: {
        "success": int,
        "skipped": int,
        "errors": int,
        "error_type": Optional[str],
        "failed_foods": List[str]
    }
    """
    food_names = [f["name"] for f in foods]
    print(f"\n{'='*60}")
    print(f"📦 Batch {batch_num}/{total_batches} ({len(foods)} foods)")
    print(f"{'='*60}")
    print(f"🍽️  Foods: {', '.join(food_names[:5])}{' ...' if len(food_names) > 5 else ''}")

    # Generate tags for entire batch with retry
    print("🤖 Generating tags from LLM (bulk)...")
    tags_by_food, error_type = generate_tags_for_foods_bulk(foods)

    if not tags_by_food:
        print(f"  ❌ Batch failed with error: {error_type}")
        return {
            "success": 0,
            "skipped": len(foods),
            "errors": len(foods),
            "error_type": error_type,
            "failed_foods": food_names
        }

    # Count generated tags
    total_tags = sum(len(tags) for tags in tags_by_food.values())
    print(f"  ✅ Generated {total_tags} tags for {len(tags_by_food)} foods")

    # Display sample tags
    for food_name, tags in list(tags_by_food.items())[:2]:
        print(f"  📝 {food_name}:")
        for tag in tags[:3]:
            print(f"      - {tag['name']} ({tag.get('category', 'N/A')})")
        if len(tags) > 3:
            print(f"      ... and {len(tags) - 3} more")

    if dry_run:
        print("  🔍 DRY RUN: Skipping database insert")
        return {
            "success": total_tags,
            "skipped": 0,
            "errors": 0,
            "error_type": None,
            "failed_foods": []
        }

    # Database operations with error handling
    try:
        # Prepare all tags for bulk insert
        print("💾 Inserting tags into database (bulk)...")
        all_tags = []
        for tags in tags_by_food.values():
            all_tags.extend(tags)

        # Bulk insert tags and get IDs
        tag_name_to_id = bulk_insert_or_get_tags(conn, all_tags)
        print(f"  ✅ Processed {len(tag_name_to_id)} unique tags")

        # Prepare food-tag relationships
        food_tags_data = []
        foods_by_name = {f["name"]: f for f in foods}

        for food_name, tags in tags_by_food.items():
            if food_name not in foods_by_name:
                print(f"  ⚠️  Food '{food_name}' not found in batch (LLM hallucination?)")
                continue

            food_id = foods_by_name[food_name]["id"]
            for tag in tags:
                tag_name = tag["name"]
                if tag_name in tag_name_to_id:
                    tag_id = tag_name_to_id[tag_name]
                    confidence = tag.get("confidence", 0.0)
                    food_tags_data.append((food_id, tag_id, confidence))

        # Bulk insert food-tag relationships
        if food_tags_data:
            bulk_insert_food_tags(conn, food_tags_data)
            print(f"  ✅ Created {len(food_tags_data)} food-tag relationships")

        # Commit transaction for this batch
        conn.commit()

        return {
            "success": len(food_tags_data),
            "skipped": 0,
            "errors": 0,
            "error_type": None,
            "failed_foods": []
        }

    except Exception as e:
        print(f"  ❌ Database error: {e}")
        import traceback
        traceback.print_exc()

        # Rollback this batch
        conn.rollback()

        return {
            "success": 0,
            "skipped": 0,
            "errors": len(foods),
            "error_type": "database_error",
            "failed_foods": food_names
        }


# ============================================
# Main Processing
# ============================================

def main():
    """Main execution function"""
    parser = argparse.ArgumentParser(description="Generate food tags using LLM (Bulk Version)")
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Run without inserting into database"
    )
    parser.add_argument(
        "--limit",
        type=int,
        help="Limit number of foods to process"
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=10,
        help="Number of foods per API call (default: 10)"
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=1.0,
        help="Delay between batch API calls in seconds (default: 1.0)"
    )
    parser.add_argument(
        "--skip-processed",
        action="store_true",
        help="Skip foods that already have tags (efficient retry)"
    )
    parser.add_argument(
        "--save-failed",
        type=str,
        help="Save failed food IDs to file for later retry"
    )

    args = parser.parse_args()

    print("🚀 Food Tag Generation Script (Bulk Processing)")
    print(f"{'='*60}")
    print(f"Database: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['dbname']}")
    print(f"Dry Run: {args.dry_run}")
    print(f"Limit: {args.limit or 'None'}")
    print(f"Batch Size: {args.batch_size} foods per API call")
    print(f"API Delay: {args.delay}s")
    print(f"Skip Processed: {args.skip_processed}")
    if args.save_failed:
        print(f"Save Failed To: {args.save_failed}")
    print(f"{'='*60}\n")

    # Connect to database
    try:
        conn = get_db_connection()
        print("✅ Database connection established\n")
    except Exception as e:
        print(f"❌ Failed to connect to database: {e}")
        sys.exit(1)

    try:
        # Fetch foods
        foods = fetch_all_foods(conn, args.limit, args.skip_processed)
        total_foods = len(foods)

        if total_foods == 0:
            if args.skip_processed:
                print("✅ All foods already have tags - nothing to process!")
            else:
                print("⚠️  No foods found in database")
            return

        if args.skip_processed:
            print(f"📊 Found {total_foods} unprocessed food(s) to process")
        else:
            print(f"📊 Found {total_foods} food(s) to process")

        # Split into batches
        batches = [
            foods[i:i + args.batch_size]
            for i in range(0, total_foods, args.batch_size)
        ]
        total_batches = len(batches)

        print(f"📦 Split into {total_batches} batches\n")

        # Process each batch
        total_stats = {"success": 0, "skipped": 0, "errors": 0}
        failed_batches = []
        error_type_counts = {}
        start_time = time.time()

        for batch_num, batch in enumerate(batches, 1):
            stats = process_food_batch(conn, batch, batch_num, total_batches, args.dry_run)

            # Update totals
            for key in ["success", "skipped", "errors"]:
                total_stats[key] += stats.get(key, 0)

            # Track failed batches
            if stats.get("errors", 0) > 0:
                failed_batches.append({
                    "batch_num": batch_num,
                    "error_type": stats.get("error_type"),
                    "failed_foods": stats.get("failed_foods", [])
                })

                # Count error types
                error_type = stats.get("error_type", "unknown")
                error_type_counts[error_type] = error_type_counts.get(error_type, 0) + 1

            # Delay between batches (except for last batch)
            if batch_num < total_batches:
                time.sleep(args.delay)

        elapsed_time = time.time() - start_time
        success_rate = (total_stats["success"] / (total_stats["success"] + total_stats["errors"]) * 100) if (total_stats["success"] + total_stats["errors"]) > 0 else 0

        # Summary
        print(f"\n{'='*60}")
        print("📊 Summary")
        print(f"{'='*60}")
        print(f"Total Foods Processed: {total_foods}")
        print(f"Total Batches: {total_batches}")
        print(f"  ✅ Successful Batches: {total_batches - len(failed_batches)}")
        print(f"  ❌ Failed Batches: {len(failed_batches)}")
        print(f"\nResults:")
        print(f"  Tags Created/Linked: {total_stats['success']}")
        print(f"  Skipped: {total_stats['skipped']}")
        print(f"  Errors: {total_stats['errors']}")
        print(f"  Success Rate: {success_rate:.1f}%")
        print(f"\nPerformance:")
        print(f"  Elapsed Time: {elapsed_time:.2f} seconds")
        print(f"  Average Speed: {total_foods / elapsed_time:.2f} foods/second")

        # Error breakdown
        if error_type_counts:
            print(f"\nError Types:")
            for error_type, count in error_type_counts.items():
                print(f"  - {error_type}: {count} batch(es)")

        # Failed batches detail
        if failed_batches and len(failed_batches) <= 5:
            print(f"\nFailed Batches Detail:")
            for failed in failed_batches:
                print(f"  Batch {failed['batch_num']}: {failed['error_type']}")
                foods = failed['failed_foods'][:3]
                print(f"    Foods: {', '.join(foods)}{'...' if len(failed['failed_foods']) > 3 else ''}")
        elif failed_batches:
            print(f"\n⚠️  {len(failed_batches)} batches failed. Run with --verbose for details.")

        print(f"{'='*60}\n")

        # Save failed foods to file
        if args.save_failed and failed_batches:
            try:
                # Collect all failed food IDs
                failed_food_ids = []
                foods_by_name = {f["name"]: f["id"] for f in foods}

                for batch in failed_batches:
                    for food_name in batch['failed_foods']:
                        if food_name in foods_by_name:
                            failed_food_ids.append(foods_by_name[food_name])

                # Save to JSON file
                with open(args.save_failed, 'w') as f:
                    json.dump({
                        "timestamp": datetime.now().isoformat(),
                        "total_failed": len(failed_food_ids),
                        "failed_food_ids": failed_food_ids,
                        "error_summary": error_type_counts
                    }, f, indent=2)

                print(f"💾 Saved {len(failed_food_ids)} failed food IDs to: {args.save_failed}\n")
            except Exception as e:
                print(f"⚠️  Failed to save failed foods: {e}\n")

        if args.dry_run:
            print("🔍 DRY RUN completed - No changes made to database")
        else:
            if total_stats['errors'] == 0:
                print("✅ All batches completed successfully!")
            elif total_stats['success'] > 0:
                print("⚠️  Partial success - some batches failed but others completed")
                if args.save_failed and failed_batches:
                    print(f"   💡 Retry with: make tags-retry")
            else:
                print("❌ All batches failed - no tags were created")
                if args.save_failed and failed_batches:
                    print(f"   💡 Retry with: make tags-retry")

    except KeyboardInterrupt:
        print("\n\n⚠️  Process interrupted by user")
        conn.rollback()
    except Exception as e:
        print(f"\n❌ Fatal error: {e}")
        import traceback
        traceback.print_exc()
        conn.rollback()
        sys.exit(1)
    finally:
        conn.close()
        print("\n🔌 Database connection closed")


if __name__ == "__main__":
    main()
