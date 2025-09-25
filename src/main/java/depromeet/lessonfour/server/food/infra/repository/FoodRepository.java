package depromeet.lessonfour.server.food.infra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import depromeet.lessonfour.server.food.domain.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

  List<Food> findAllByIdIn(List<Long> foodIds);

  @Query(value = "SELECT * FROM foods ORDER BY RANDOM() LIMIT 5", nativeQuery = true)
  List<Food> findRandomFoods();

  // 부분 문자열 검색 (trigram 인덱스 활용)
  @Query(
      value =
          "SELECT * FROM foods WHERE name ILIKE %:name% ORDER BY usage_count DESC, name LIMIT :count",
      nativeQuery = true)
  List<Food> findByNameContaining(@Param("name") String name, @Param("count") int count);

  // 사용량 증가 메서드
  @Modifying
  @Query("UPDATE Food f SET f.usage_count = f.usage_count + 1 WHERE f.id = :id")
  void incrementUsageCount(@Param("id") Long id);
}
