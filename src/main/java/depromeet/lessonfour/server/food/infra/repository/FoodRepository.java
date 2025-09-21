package depromeet.lessonfour.server.food.infra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import depromeet.lessonfour.server.food.domain.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

  @Query(value = "SELECT * FROM foods ORDER BY RANDOM() LIMIT 5", nativeQuery = true)
  List<Food> findRandomFoods();
}
